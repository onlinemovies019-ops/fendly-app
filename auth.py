import base64
import asyncio
import hashlib
import hmac
import json
import os
import random
import logging
import smtplib
import ssl
import time
from email.message import EmailMessage
from typing import Any

import firebase_admin
import httpx
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from firebase_admin import auth as firebase_auth
from firebase_admin import credentials
from pydantic import BaseModel, Field


bearer_scheme = HTTPBearer(auto_error=False)
router = APIRouter(prefix="/api/auth", tags=["auth"])
logger = logging.getLogger(__name__)
_EMAIL_OTP_STORE: dict[str, dict[str, Any]] = {}
_EMAIL_OTP_TTL_SECONDS = 300


class SendOtpRequest(BaseModel):
    phone_number: str = Field(..., min_length=10)


class VerifyOtpRequest(BaseModel):
    session_id: str = Field(..., min_length=1)
    otp: str = Field(..., min_length=4, max_length=8)


class SendEmailOtpRequest(BaseModel):
    email: str = Field(..., min_length=3)


class VerifyEmailOtpRequest(BaseModel):
    email: str = Field(..., min_length=3)
    otp: str = Field(..., min_length=4, max_length=8)


def _app_secret() -> str:
    return os.getenv("APP_SECRET_KEY") or os.getenv("JWT_SECRET_KEY") or "fendly-dev-secret"


def _b64url_encode(value: bytes) -> str:
    return base64.urlsafe_b64encode(value).rstrip(b"=").decode("ascii")


def _b64url_decode(value: str) -> bytes:
    padding = "=" * (-len(value) % 4)
    return base64.urlsafe_b64decode((value + padding).encode("ascii"))


def create_session_token(subject: str, claims: dict[str, Any] | None = None) -> str:
    header = {"alg": "HS256", "typ": "JWT"}
    now = int(__import__("time").time())
    payload = {"sub": subject, "iat": now, "exp": now + 3600}
    if claims:
        payload.update(claims)
    header_segment = _b64url_encode(json.dumps(header, separators=(",", ":")).encode("utf-8"))
    payload_segment = _b64url_encode(json.dumps(payload, separators=(",", ":")).encode("utf-8"))
    signing_input = f"{header_segment}.{payload_segment}".encode("ascii")
    signature = hmac.new(_app_secret().encode("utf-8"), signing_input, hashlib.sha256).digest()
    return f"{header_segment}.{payload_segment}.{_b64url_encode(signature)}"


def verify_session_token(token: str) -> dict[str, Any]:
    try:
        header_segment, payload_segment, signature_segment = token.split(".")
    except ValueError as exc:
        raise ValueError("Invalid JWT format") from exc
    signing_input = f"{header_segment}.{payload_segment}".encode("ascii")
    expected = _b64url_encode(
        hmac.new(_app_secret().encode("utf-8"), signing_input, hashlib.sha256).digest()
    )
    if not hmac.compare_digest(expected, signature_segment):
        raise ValueError("Invalid JWT signature")
    payload = json.loads(_b64url_decode(payload_segment).decode("utf-8"))
    expires_at = payload.get("exp")
    if expires_at is not None and int(expires_at) < int(__import__("time").time()):
        raise ValueError("JWT expired")
    return payload


def _generate_email_otp() -> str:
    return "".join(str(random.randint(0, 9)) for _ in range(6))


def _email_otp_debug_mode_enabled() -> bool:
    if os.getenv("EMAIL_OTP_DEBUG_MODE", "").strip().lower() == "true":
        return True
    environment = (os.getenv("ENVIRONMENT") or "").strip().lower()
    return environment not in {"", "production", "prod"}


def _send_email_otp_code(email: str, otp: str) -> None:
    smtp_host = (os.getenv("SMTP_HOST") or "").strip()
    smtp_port = int((os.getenv("SMTP_PORT") or "587").strip())
    smtp_username = (os.getenv("SMTP_USERNAME") or "").strip()
    smtp_password = (os.getenv("SMTP_PASSWORD") or "").replace(" ", "")
    smtp_from = (os.getenv("SMTP_FROM_EMAIL") or smtp_username or "noreply@localhost").strip()

    if not smtp_host or not smtp_username or not smtp_password:
        if _email_otp_debug_mode_enabled():
            return
        raise RuntimeError("SMTP credentials are not configured")

    msg = EmailMessage()
    msg["Subject"] = "Fendly verification code"
    msg["From"] = smtp_from
    msg["To"] = email
    msg.set_content(
        f"Your Fendly verification code is {otp}. It is valid for 5 minutes. "
        "Do not share this code with anyone."
    )

    tls_context = ssl.create_default_context()
    if smtp_port == 465:
        with smtplib.SMTP_SSL(smtp_host, smtp_port, timeout=20) as server:
            server.ehlo()
            server.login(smtp_username, smtp_password)
            server.send_message(msg)
        return

    with smtplib.SMTP(smtp_host, smtp_port, timeout=20) as server:
        server.ehlo()
        server.starttls(context=tls_context)
        server.ehlo()
        server.login(smtp_username, smtp_password)
        server.send_message(msg)


def _firebase_app() -> firebase_admin.App:
    if firebase_admin._apps:
        return firebase_admin.get_app()
    raw_credentials = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
    if raw_credentials:
        return firebase_admin.initialize_app(credentials.Certificate(json.loads(raw_credentials)))
    credentials_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    if credentials_path:
        return firebase_admin.initialize_app(credentials.Certificate(credentials_path))
    return firebase_admin.initialize_app(credentials.ApplicationDefault())


@router.post("/send-otp")
async def send_otp(payload: SendOtpRequest) -> dict[str, Any]:
    api_key = os.getenv("TWOFACTOR_API_KEY")
    if not api_key:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="TWOFACTOR_API_KEY is not configured")
    phone_number = payload.phone_number.strip()
    if not phone_number:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="phone_number is required")
    url = f"https://2factor.in/API/V1/{api_key}/SMS/{phone_number}/AUTOGEN"
    try:
        async with httpx.AsyncClient(timeout=20.0) as client:
            response = await client.get(url)
            response.raise_for_status()
            data = response.json()
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY, detail="Could not contact 2Factor.in") from exc
    if data.get("Status") != "Success":
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=data.get("Details") or "Unable to send OTP")
    session_id = data.get("Details")
    if not session_id:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY, detail="2Factor response did not include a session ID")
    return {"success": True, "session_id": session_id}


@router.post("/verify-otp")
async def verify_otp(payload: VerifyOtpRequest) -> dict[str, Any]:
    api_key = os.getenv("TWOFACTOR_API_KEY")
    if not api_key:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="TWOFACTOR_API_KEY is not configured")
    session_id = payload.session_id.strip()
    otp = payload.otp.strip()
    if not session_id or not otp:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="session_id and otp are required")
    url = f"https://2factor.in/API/V1/{api_key}/SMS/VERIFY/{session_id}/{otp}"
    try:
        async with httpx.AsyncClient(timeout=20.0) as client:
            response = await client.get(url)
            response.raise_for_status()
            data = response.json()
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY, detail="Could not verify OTP with 2Factor.in") from exc

    if data.get("Status") != "Success" or str(data.get("Details", "")).strip().lower() != "otp matched":
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired OTP")

    subject = f"2factor:{session_id}"
    token = create_session_token(subject, {"session_id": session_id})
    return {"success": True, "token": token}


@router.post("/send-email-otp")
async def send_email_otp(payload: SendEmailOtpRequest) -> dict[str, Any]:
    email = payload.email.strip().lower()
    if not email:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="email is required")

    otp = _generate_email_otp()
    _EMAIL_OTP_STORE[email] = {
        "otp": otp,
        "expires_at": time.time() + _EMAIL_OTP_TTL_SECONDS,
    }
    try:
        await asyncio.to_thread(_send_email_otp_code, email, otp)
    except Exception as exc:
        _EMAIL_OTP_STORE.pop(email, None)
        logger.exception("Email OTP delivery failed for %s", email)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Email delivery failed: {type(exc).__name__}: {str(exc)[:180]}",
        ) from exc

    if _email_otp_debug_mode_enabled() and (not os.getenv("SMTP_HOST") or not os.getenv("SMTP_USERNAME") or not os.getenv("SMTP_PASSWORD")):
        return {
            "success": True,
            "message": "OTP sent to email (debug mode)",
            "expires_in_seconds": _EMAIL_OTP_TTL_SECONDS,
            "debug_otp": otp,
        }

    return {"success": True, "message": "OTP sent to email", "expires_in_seconds": _EMAIL_OTP_TTL_SECONDS}


@router.post("/verify-email-otp")
async def verify_email_otp(payload: VerifyEmailOtpRequest) -> dict[str, Any]:
    email = payload.email.strip().lower()
    otp = payload.otp.strip()
    if not email or not otp:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="email and otp are required")

    stored = _EMAIL_OTP_STORE.get(email)
    if not stored or stored["expires_at"] < time.time():
        _EMAIL_OTP_STORE.pop(email, None)
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired OTP")

    if stored["otp"] != otp:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired OTP")

    _EMAIL_OTP_STORE.pop(email, None)
    token = create_session_token(f"email:{email}", {"email": email})
    return {"success": True, "token": token}


def get_current_user(
    token: HTTPAuthorizationCredentials | None = Depends(bearer_scheme),
) -> str:
    if token is None or token.scheme.lower() != "bearer":
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Bearer token required")
    try:
        _firebase_app()
        decoded_token = firebase_auth.verify_id_token(token.credentials)
        return decoded_token["uid"]
    except Exception:
        try:
            payload = verify_session_token(token.credentials)
            subject = payload.get("sub")
            if not subject:
                raise ValueError("Missing sub claim")
            return subject
        except Exception as exc:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid authentication token") from exc