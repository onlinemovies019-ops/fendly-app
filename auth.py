import os
import json
import urllib.request
from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import httpx
from pydantic import BaseModel, EmailStr

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

TWO_FACTOR_API_KEY = os.getenv("TWO_FACTOR_API_KEY", "")
RESEND_API_KEY = os.getenv("RESEND_API_KEY", "")
RESEND_FROM_EMAIL = os.getenv("RESEND_FROM_EMAIL", "onboarding@resend.dev")

security = HTTPBearer(auto_error=False)

async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)):
    """
    Authentication dependency required by protected endpoints (e.g., in `routers/items.py`).
    """
    if not credentials:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization token missing or invalid",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return {"token": credentials.credentials, "user": "authenticated_user"}

# — Schemas —

class SendOTPRequest(BaseModel):
    phone_number: str

class VerifyOTPRequest(BaseModel):
    session_id: str
    otp: str

class EmailOTPRequest(BaseModel):
    email: EmailStr
    otp: str

# — Endpoints —

@router.post("/send-otp")
async def send_otp(payload: SendOTPRequest):
    if not TWO_FACTOR_API_KEY or TWO_FACTOR_API_KEY == "your_2factor_api_key":
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="TWO_FACTOR_API_KEY is not configured in Render Environment Variables"
        )

    raw_phone = payload.phone_number.replace(" ", "").replace("-", "").replace("+", "")
    if len(raw_phone) > 10 and raw_phone.startswith("91"):
        clean_phone = raw_phone
    elif len(raw_phone) == 10:
        clean_phone = "91" + raw_phone
    else:
        clean_phone = raw_phone

    url = f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/{clean_phone}/AUTOGEN"
    print(f"2Factor Send OTP URL: {url.replace(TWO_FACTOR_API_KEY, 'REDACTED')}")

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, timeout=15.0)
            data = response.json()
            print(f"2Factor Response: {data}")

            if data.get("Status") == "Success":
                return {
                    "status": "success",
                    "session_id": data.get("Details"),
                    "message": "OTP sent successfully via 2Factor"
                }
            else:
                detail_msg = data.get("Details", "Failed to send OTP via 2Factor")
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"2Factor error: {detail_msg}"
                )
        except httpx.HTTPError as he:
            print(f"2Factor HTTP Error: {he}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"2Factor network error: {str(e if 'e' in locals() else he)}"
            )
        except Exception as e:
            print(f"2Factor Error: {e}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"SMS error: {str(e)}"
            )

@router.post("/verify-otp")
async def verify_otp(payload: VerifyOTPRequest):
    if not TWO_FACTOR_API_KEY or TWO_FACTOR_API_KEY == "your_2factor_api_key":
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="TWO_FACTOR_API_KEY is not configured in Render Environment Variables"
        )

    url = f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/VERIFY/{payload.session_id}/{payload.otp}"
    print(f"2Factor Verify URL: {url.replace(TWO_FACTOR_API_KEY, 'REDACTED')}")

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, timeout=15.0)
            data = response.json()
            print(f"2Factor Verify Response: {data}")

            if data.get("Status") == "Success" and (
                data.get("Details") == "OTP Matched" or "Matched" in str(data.get("Details"))
            ):
                return {"status": "success", "message": "OTP verified successfully"}
            else:
                detail_msg = data.get("Details", "Invalid or expired OTP")
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"2Factor verification error: {detail_msg}"
                )
        except Exception as e:
            print(f"2Factor Verify Exception: {e}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Verification failed: {str(e)}"
            )

@router.post("/send-email-otp")
async def send_email_otp(payload: EmailOTPRequest):
    if not RESEND_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="RESEND_API_KEY is not configured on server"
        )
    headers = {
        "Authorization": f"Bearer {RESEND_API_KEY}",
        "Content-Type": "application/json"
    }
    email_body = {
        "from": RESEND_FROM_EMAIL,
        "to": [payload.email],
        "subject": "Your Verification Code",
        "html": f"""
            <div style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>Verification Code</h2>
                <p>Your OTP code is: <strong style="font-size: 24px; color: #4F46E5;">{payload.otp}</strong></p>
                <p>This code is valid for 10 minutes.</p>
            </div>
        """
    }
    try:
        req = urllib.request.Request(
            "https://api.resend.com/emails",
            data=json.dumps(email_body).encode("utf-8"),
            headers=headers,
            method="POST"
        )
        with urllib.request.urlopen(req, timeout=10.0) as resp:
            if resp.status in [200, 201]:
                return {"status": "success", "message": "Email OTP sent successfully"}
            else:
                raise HTTPException(
                    status_code=resp.status,
                    detail="Resend error"
                )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to send email: {str(e)}"
        )
