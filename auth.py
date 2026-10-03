import os
import json
import hashlib
import hmac
import re
import secrets
import time
from typing import Any
from urllib.parse import quote
from fastapi import APIRouter, Depends, HTTPException, Request, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import firebase_admin
from firebase_admin import auth as firebase_auth
from firebase_admin import credentials as firebase_credentials
import httpx
from pydantic import BaseModel, EmailStr, Field
from sqlalchemy import delete, select
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session

from database import get_db
from models import EmailOTPChallenge, SmsOTPChallenge, SmsOTPRateLimit, User
from profile_service import update_profile_record
from schemas import ProfileUpdate

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

TWO_FACTOR_API_KEY = os.getenv("TWO_FACTOR_API_KEY") or os.getenv("TWOFACTOR_API_KEY", "")
RESEND_API_KEY = os.getenv("RESEND_API_KEY", "")
RESEND_FROM_EMAIL = (
    os.getenv("RESEND_FROM_EMAIL")
    or os.getenv("EMAIL_FROM")
    or os.getenv("SMTP_FROM_EMAIL")
    or "onboarding@resend.dev"
)

EMAIL_OTP_TTL_SECONDS = 600
EMAIL_OTP_RESEND_SECONDS = 60
EMAIL_OTP_MAX_ATTEMPTS = 5
EMAIL_OTP_MAX_SENDS_PER_DAY = 10
SMS_OTP_RESEND_SECONDS = 60
SMS_OTP_MAX_SENDS_PER_DAY = 5
SMS_OTP_MAX_IP_SENDS_PER_HOUR = 10
SMS_OTP_MAX_IP_SENDS_PER_DAY = 20
SMS_OTP_TTL_SECONDS = 600
SMS_OTP_MAX_VERIFY_ATTEMPTS = 5

print(f"=== AUTH ROUTER LOADED ===", flush=True)
print(f"TWO_FACTOR_API_KEY present: {bool(TWO_FACTOR_API_KEY)}, length: {len(TWO_FACTOR_API_KEY)}", flush=True)
print(f"RESEND_API_KEY present: {bool(RESEND_API_KEY)}, length: {len(RESEND_API_KEY)}", flush=True)

security = HTTPBearer(auto_error=False)


def _firebase_app():
    try:
        return firebase_admin.get_app()
    except ValueError:
        service_account_json = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
        if service_account_json:
            return firebase_admin.initialize_app(
                firebase_credentials.Certificate(json.loads(service_account_json))
            )
        return firebase_admin.initialize_app()


def _email_otp_digest(uid: str, email: str, otp: str) -> str:
    key = os.getenv("APP_SECRET_KEY", "")
    if len(key) < 32:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Email verification is not configured",
        )
    message = f"{uid}:{email}:{otp}".encode("utf-8")
    return hmac.new(key.encode("utf-8"), message, hashlib.sha256).hexdigest()


async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    """
    Authentication dependency required by protected endpoints.
    Extracts and returns the user UID (string) from the Bearer token.
    """
    if not credentials:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization token missing or invalid",
            headers={"WWW-Authenticate": "Bearer"},
        )
    try:
        decoded_token = firebase_auth.verify_id_token(
            credentials.credentials,
            app=_firebase_app(),
            check_revoked=True,
        )
    except (firebase_auth.InvalidIdTokenError, firebase_auth.ExpiredIdTokenError,
            firebase_auth.RevokedIdTokenError, firebase_auth.UserDisabledError) as exc:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization token is invalid or expired",
            headers={"WWW-Authenticate": "Bearer"},
        ) from exc
    except firebase_admin.exceptions.FirebaseError as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Authentication service is temporarily unavailable",
        ) from exc
    except (ValueError, TypeError, json.JSONDecodeError) as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Authentication service is not configured",
        ) from exc

    uid = decoded_token.get("uid")
    if not isinstance(uid, str) or not uid:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization token has no user identity",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return uid


def normalize_profile_photo(value: Any) -> str:
    if value is None:
        return ""
    text = str(value).strip()
    return text


@router.get("/profile")
def get_profile_compat(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, Any]:
    try:
        user = session.scalar(select(User).where(User.firebase_uid == uid))
        if user is None:
            user = User(firebase_uid=uid, email_verified=False, mobile_verified=False)
            session.add(user)
            try:
                session.commit()
                session.refresh(user)
            except Exception:
                session.rollback()
        return {
            "username": getattr(user, "username", None) or "",
            "full_name": getattr(user, "full_name", None) or "",
            "email": getattr(user, "email", None) or "",
            "mobile": getattr(user, "mobile", None) or "",
            "state": getattr(user, "state", None) or "",
            "city": getattr(user, "city", None) or "",
            "profile_photo_url": normalize_profile_photo(getattr(user, "profile_photo_url", None)),
            "email_verified": bool(getattr(user, "email_verified", False)),
            "mobile_verified": bool(getattr(user, "mobile_verified", False)),
            "is_verified": bool(getattr(user, "email_verified", False)),
        }
    except Exception:
        return {
            "username": "",
            "full_name": "",
            "email": "",
            "mobile": "",
            "state": "",
            "city": "",
            "profile_photo_url": "",
            "email_verified": False,
            "mobile_verified": False,
            "is_verified": False,
        }


@router.put("/profile")
def update_profile_compat(
    payload: ProfileUpdate,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, object]:
    try:
        update_profile_record(session, payload, uid)
        return {"status": "saved"}
    except HTTPException:
        session.rollback()
        raise
    except Exception as exc:
        session.rollback()
        raise HTTPException(status_code=500, detail=f"Failed to update profile: {exc}") from exc

# — Schemas —

class SendOTPRequest(BaseModel):
    phone_number: str = Field(min_length=10, max_length=16)

class VerifyOTPRequest(BaseModel):
    session_id: str = Field(min_length=1, max_length=256)
    otp: str = Field(min_length=6, max_length=6, pattern=r"^\d{6}$")

class EmailOTPRequest(BaseModel):
    email: EmailStr

class VerifyEmailOTPRequest(BaseModel):
    email: EmailStr
    otp: str = Field(min_length=6, max_length=6)

# — Endpoints —

@router.post("/send-otp")
async def send_otp(
    payload: SendOTPRequest,
    request: Request,
    session: Session = Depends(get_db),
) -> dict[str, str | bool]:
    if not TWO_FACTOR_API_KEY or TWO_FACTOR_API_KEY == "your_2factor_api_key":
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="SMS verification is not configured",
        )

    raw_phone = re.sub(r"[\s()+-]", "", payload.phone_number)
    if raw_phone.startswith("91") and len(raw_phone) == 12:
        local_phone = raw_phone[2:]
    elif len(raw_phone) == 10:
        local_phone = raw_phone
    else:
        local_phone = ""
    if not re.fullmatch(r"[6-9]\d{9}", local_phone):
        raise HTTPException(status_code=422, detail="Enter a valid Indian mobile number")

    if request.client is None or not request.client.host:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="SMS verification is temporarily unavailable",
        )
    now = int(time.time())
    phone_digest = _sms_otp_digest("phone", local_phone)
    try:
        _reserve_sms_otp_quota(
            session,
            phone_digest=phone_digest,
            client_ip=request.client.host,
            now=now,
        )
    except HTTPException:
        raise
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="SMS verification is temporarily unavailable",
        ) from exc

    clean_phone = f"91{local_phone}"
    url = f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/{clean_phone}/AUTOGEN"

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, timeout=15.0)
            response.raise_for_status()
            data = response.json()
        except (httpx.HTTPError, ValueError) as exc:
            raise HTTPException(
                status_code=status.HTTP_502_BAD_GATEWAY,
                detail="Could not send SMS verification code",
            ) from exc

    session_id = (
        data.get("Details")
        if isinstance(data, dict) and data.get("Status") == "Success"
        else None
    )
    if not isinstance(session_id, str) or not session_id or len(session_id) > 256:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="Could not send SMS verification code",
        )
    session_digest = _sms_otp_digest("session", session_id)
    try:
        session.execute(
            delete(SmsOTPChallenge).where(SmsOTPChallenge.phone_digest == phone_digest)
        )
        session.add(
            SmsOTPChallenge(
                session_digest=session_digest,
                phone_digest=phone_digest,
                sent_at=now,
                expires_at=now + SMS_OTP_TTL_SECONDS,
                attempts=0,
            )
        )
        session.commit()
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="SMS code was sent but verification could not be prepared",
        ) from exc
    return {
        "success": True,
        "status": "success",
        "session_id": session_id,
        "message": "OTP sent successfully",
    }

@router.post("/verify-otp")
async def verify_otp(
    payload: VerifyOTPRequest,
    session: Session = Depends(get_db),
) -> dict[str, str | bool]:
    if not TWO_FACTOR_API_KEY or TWO_FACTOR_API_KEY == "your_2factor_api_key":
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="SMS verification is not configured",
        )

    now = int(time.time())
    session_digest = _sms_otp_digest("session", payload.session_id)
    try:
        challenge = session.scalar(
            select(SmsOTPChallenge)
            .where(SmsOTPChallenge.session_digest == session_digest)
            .with_for_update()
        )
        if challenge is None or challenge.expires_at < now:
            if challenge is not None:
                session.delete(challenge)
                session.commit()
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid or expired verification session",
            )
        if challenge.attempts >= SMS_OTP_MAX_VERIFY_ATTEMPTS:
            session.delete(challenge)
            session.commit()
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="Too many verification attempts; request a new code",
            )
        challenge.attempts += 1
        session.commit()
    except HTTPException:
        raise
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="SMS verification is temporarily unavailable",
        ) from exc

    url = (
        f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/VERIFY/"
        f"{quote(payload.session_id, safe='')}/{payload.otp}"
    )

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, timeout=15.0)
            response.raise_for_status()
            data = response.json()
        except (httpx.HTTPError, ValueError) as exc:
            raise HTTPException(
                status_code=status.HTTP_502_BAD_GATEWAY,
                detail="Could not verify SMS code",
            ) from exc

    verified = isinstance(data, dict) and data.get("Status") == "Success" and (
        data.get("Details") == "OTP Matched"
        or "Matched" in str(data.get("Details"))
    )
    try:
        challenge = session.get(SmsOTPChallenge, session_digest)
        if challenge is None:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid or expired verification session",
            )
        if verified:
            session.delete(challenge)
        elif challenge.attempts >= SMS_OTP_MAX_VERIFY_ATTEMPTS:
            session.delete(challenge)
        session.commit()
    except HTTPException:
        session.rollback()
        raise
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="SMS verification is temporarily unavailable",
        ) from exc

    if not verified:
        return {"success": False, "status": "error", "message": "Invalid or expired OTP"}
    return {"success": True, "status": "success", "message": "OTP verified successfully"}


def _sms_otp_digest(purpose: str, value: str) -> str:
    key = os.getenv("APP_SECRET_KEY", "")
    if len(key) < 32:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="SMS verification is not configured",
        )
    message = f"{purpose}:{value}".encode("utf-8")
    return hmac.new(key.encode("utf-8"), message, hashlib.sha256).hexdigest()


def _reserve_sms_otp_quota(
    session: Session,
    *,
    phone_digest: str,
    client_ip: str,
    now: int,
) -> None:
    phone_key = _sms_otp_digest("phone-quota", phone_digest)
    ip_hour_key = _sms_otp_digest("ip-hour-quota", client_ip)
    ip_day_key = _sms_otp_digest("ip-day-quota", client_ip)
    limits = (
        (phone_key, SMS_OTP_RESEND_SECONDS, 86400, SMS_OTP_MAX_SENDS_PER_DAY),
        (ip_hour_key, 0, 3600, SMS_OTP_MAX_IP_SENDS_PER_HOUR),
        (ip_day_key, 0, 86400, SMS_OTP_MAX_IP_SENDS_PER_DAY),
    )
    session.execute(
        delete(SmsOTPChallenge).where(SmsOTPChallenge.expires_at <= now)
    )
    session.execute(
        delete(SmsOTPRateLimit).where(SmsOTPRateLimit.sent_at <= now - 86400)
    )
    rows = {
        row.quota_key: row
        for row in session.scalars(
            select(SmsOTPRateLimit)
            .where(SmsOTPRateLimit.quota_key.in_([key for key, *_ in limits]))
            .with_for_update()
        ).all()
    }
    for key, cooldown, window_seconds, max_sends in limits:
        row = rows.get(key)
        if row is None:
            continue
        if cooldown and now - row.sent_at < cooldown:
            session.rollback()
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="Please wait before requesting another verification code",
            )
        count = row.send_count if now - row.window_started < window_seconds else 0
        if count >= max_sends:
            session.rollback()
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="SMS verification request limit reached",
            )

    for key, _, window_seconds, _ in limits:
        row = rows.get(key)
        if row is None:
            session.add(
                SmsOTPRateLimit(
                    quota_key=key,
                    sent_at=now,
                    window_started=now,
                    send_count=1,
                )
            )
        else:
            if now - row.window_started >= window_seconds:
                row.window_started = now
                row.send_count = 0
            row.sent_at = now
            row.send_count += 1
    session.commit()

@router.post("/send-email-otp")
async def send_email_otp(
    payload: EmailOTPRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str | bool]:
    if not RESEND_API_KEY or RESEND_API_KEY == "your_resend_api_key":
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Email verification is not configured",
        )

    email = str(payload.email).strip().lower()
    now = int(time.time())
    try:
        session.execute(
            delete(EmailOTPChallenge).where(
                EmailOTPChallenge.expires_at <= now,
                EmailOTPChallenge.send_window_started + 86400 <= now,
            )
        )
        challenge = session.scalar(
            select(EmailOTPChallenge)
            .where(EmailOTPChallenge.firebase_uid == uid)
            .with_for_update()
        )
        if challenge and now - challenge.sent_at < EMAIL_OTP_RESEND_SECONDS:
            session.rollback()
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="Please wait before requesting another code",
            )
        window_started = challenge.send_window_started if challenge else 0
        send_count = challenge.send_count if challenge else 0
        if not window_started or now - window_started >= 86400:
            window_started = now
            send_count = 0
        if send_count >= EMAIL_OTP_MAX_SENDS_PER_DAY:
            session.rollback()
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="Daily email verification limit reached",
            )

        otp = f"{secrets.randbelow(900000) + 100000}"
        digest = _email_otp_digest(uid, email, otp)
        if challenge is None:
            challenge = EmailOTPChallenge(
                firebase_uid=uid,
                email=email,
                code_digest=digest,
                sent_at=now,
                expires_at=now + EMAIL_OTP_TTL_SECONDS,
                send_window_started=window_started,
                send_count=send_count + 1,
                attempts=0,
                sent=False,
            )
            session.add(challenge)
        else:
            challenge.email = email
            challenge.code_digest = digest
            challenge.sent_at = now
            challenge.expires_at = now + EMAIL_OTP_TTL_SECONDS
            challenge.send_window_started = window_started
            challenge.send_count = send_count + 1
            challenge.attempts = 0
            challenge.sent = False
        session.commit()
    except HTTPException:
        raise
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Email verification is temporarily unavailable",
        ) from exc

    headers = {
        "Authorization": f"Bearer {RESEND_API_KEY}",
        "Content-Type": "application/json"
    }
    email_body = {
        "from": RESEND_FROM_EMAIL,
        "to": [email],
        "subject": "Your Fendly Verification Code",
        "html": f"""
            <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; border-radius: 8px;">
                <h2 style="color: #4F46E5;">Fendly Verification Code</h2>
                <p>Hello,</p>
                <p>Your verification code is:</p>
                <div style="background-color: #ffffff; padding: 15px; border-radius: 6px; text-align: center; font-size: 28px; font-weight: bold; color: #111827; letter-spacing: 4px;">{otp}</div>
                <p style="margin-top: 20px; color: #6B7280; font-size: 14px;">This code is valid for 10 minutes. If you did not request this, please ignore this email.</p>
            </div>
        """
    }
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                "https://api.resend.com/emails",
                json=email_body,
                headers=headers,
                timeout=15.0
            )
            response.raise_for_status()
    except httpx.HTTPError as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="Could not send email verification code",
        ) from exc

    try:
        challenge = session.get(EmailOTPChallenge, uid)
        if challenge is None or challenge.code_digest != digest:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Email verification challenge changed; request a new code",
            )
        challenge.sent = True
        session.commit()
    except HTTPException:
        session.rollback()
        raise
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Email code was sent but verification could not be prepared",
        ) from exc

    return {"success": True, "status": "sent", "message": "Verification code sent"}

@router.post("/verify-email-otp")
def verify_email_otp(
    payload: VerifyEmailOTPRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str | bool]:
    email = str(payload.email).strip().lower()
    now = int(time.time())
    try:
        challenge = session.scalar(
            select(EmailOTPChallenge)
            .where(EmailOTPChallenge.firebase_uid == uid)
            .with_for_update()
        )
        if challenge is None or not challenge.sent or challenge.email != email or challenge.expires_at < now:
            if challenge and challenge.expires_at < now:
                session.delete(challenge)
                session.commit()
            else:
                session.rollback()
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired code")

        digest = _email_otp_digest(uid, email, payload.otp.strip())
        challenge.attempts += 1
        if (
            challenge.attempts > EMAIL_OTP_MAX_ATTEMPTS
            or not secrets.compare_digest(challenge.code_digest, digest)
        ):
            if challenge.attempts >= EMAIL_OTP_MAX_ATTEMPTS:
                session.delete(challenge)
            session.commit()
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired code")
    except HTTPException:
        raise
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Email verification is temporarily unavailable",
        ) from exc

    try:
        app = _firebase_app()
        firebase_auth.update_user(uid, email=email, email_verified=True, app=app)
    except firebase_auth.EmailAlreadyExistsError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="This email address is already linked to another account",
        ) from exc
    except firebase_admin.exceptions.FirebaseError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Authentication service is temporarily unavailable",
        ) from exc

    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None:
        user = User(firebase_uid=uid)
        session.add(user)
    user.email = email
    user.email_verified = True
    session.delete(challenge)
    try:
        session.commit()
    except SQLAlchemyError as exc:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Email was verified but profile status could not be saved",
        ) from exc

    return {"success": True, "status": "success", "message": "Email verified successfully"}
