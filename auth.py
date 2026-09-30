import os
import json
import random
import urllib.request
import base64
from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import httpx
from pydantic import BaseModel, EmailStr

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

TWO_FACTOR_API_KEY = os.getenv("TWO_FACTOR_API_KEY", "")
RESEND_API_KEY = os.getenv("RESEND_API_KEY", "")
RESEND_FROM_EMAIL = os.getenv("RESEND_FROM_EMAIL", "onboarding@resend.dev")

print(f"=== AUTH ROUTER LOADED ===", flush=True)
print(f"TWO_FACTOR_API_KEY present: {bool(TWO_FACTOR_API_KEY)}, length: {len(TWO_FACTOR_API_KEY)}", flush=True)
print(f"RESEND_API_KEY present: {bool(RESEND_API_KEY)}, length: {len(RESEND_API_KEY)}", flush=True)

security = HTTPBearer(auto_error=False)

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
    token = credentials.credentials
    try:
        parts = token.split(".")
        if len(parts) == 3:
            padding = '=' * (-len(parts[1]) % 4)
            payload_json = base64.b64decode(parts[1] + padding).decode('utf-8')
            payload_data = json.loads(payload_json)
            uid = payload_data.get("user_id") or payload_data.get("sub") or payload_data.get("uid") or payload_data.get("email")
            if uid:
                return str(uid)
    except Exception:
        pass
    return str(token)

# — Schemas —

class SendOTPRequest(BaseModel):
    phone_number: str

class VerifyOTPRequest(BaseModel):
    session_id: str
    otp: str

class EmailOTPRequest(BaseModel):
    email: EmailStr

# — Endpoints —

@router.post("/send-otp")
async def send_otp(payload: SendOTPRequest):
    print(f"-> RECEIVED /api/auth/send-otp with phone: {payload.phone_number}", flush=True)
    if not TWO_FACTOR_API_KEY or TWO_FACTOR_API_KEY == "your_2factor_api_key":
        print("-> ERROR: TWO_FACTOR_API_KEY is missing or unconfigured in environment!", flush=True)
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="TWO_FACTOR_API_KEY is not configured in Render Environment Variables."
        )

    raw_phone = payload.phone_number.replace(" ", "").replace("-", "").replace("+", "")
    if len(raw_phone) > 10 and raw_phone.startswith("91"):
        clean_phone = raw_phone
    elif len(raw_phone) == 10:
        clean_phone = "91" + raw_phone
    else:
        clean_phone = raw_phone

    url = f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/{clean_phone}/AUTOGEN"
    print(f"-> Calling 2Factor API URL: {url.replace(TWO_FACTOR_API_KEY, 'REDACTED')}", flush=True)

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, timeout=15.0)
            print(f"-> 2Factor HTTP Status: {response.status_code}", flush=True)
            try:
                data = response.json()
            except Exception:
                data = {"Status": "Error", "Details": f"Invalid JSON: {response.text}"}
            print(f"-> 2Factor JSON Response: {data}", flush=True)

            if data.get("Status") == "Success":
                return {
                    "success": True,
                    "status": "success",
                    "session_id": data.get("Details"),
                    "message": "OTP sent successfully via 2Factor"
                }
            else:
                detail_msg = data.get("Details", data.get("Message", "Failed to send OTP"))
                print(f"-> 2Factor Rejected OTP Request: {detail_msg}", flush=True)
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"2Factor: {detail_msg}"
                )
        except HTTPException as he:
            raise he
        except Exception as e:
            print(f"-> 2Factor Exception: {e}", flush=True)
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"2Factor Error: {str(e)}"
            )

@router.post("/verify-otp")
async def verify_otp(payload: VerifyOTPRequest):
    print(f"-> RECEIVED /api/auth/verify-otp with session_id: {payload.session_id}, otp: {payload.otp}", flush=True)
    if not TWO_FACTOR_API_KEY or TWO_FACTOR_API_KEY == "your_2factor_api_key":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="TWO_FACTOR_API_KEY is not configured in Render Environment Variables"
        )

    url = f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/VERIFY/{payload.session_id}/{payload.otp}"
    print(f"-> Calling 2Factor Verify URL: {url.replace(TWO_FACTOR_API_KEY, 'REDACTED')}", flush=True)

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, timeout=15.0)
            print(f"-> 2Factor Verify HTTP Status: {response.status_code}", flush=True)
            try:
                data = response.json()
            except Exception:
                data = {"Status": "Error", "Details": f"Invalid JSON: {response.text}"}
            print(f"-> 2Factor Verify JSON Response: {data}", flush=True)

            if data.get("Status") == "Success" and (
                data.get("Details") == "OTP Matched" or "Matched" in str(data.get("Details")) or "Success" in str(data.get("Status"))
            ):
                return {
                    "success": True,
                    "status": "success",
                    "message": "OTP verified successfully"
                }
            else:
                detail_msg = data.get("Details", "Invalid or expired OTP")
                print(f"-> 2Factor Verify REJECTED: {detail_msg}", flush=True)
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"2Factor: {detail_msg}"
                )
        except HTTPException as he:
            raise he
        except Exception as e:
            print(f"-> 2Factor Verify Exception: {e}", flush=True)
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Verification Error: {str(e)}"
            )

@router.post("/send-email-otp")
async def send_email_otp(payload: EmailOTPRequest):
    if not RESEND_API_KEY or RESEND_API_KEY == "your_resend_api_key":
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="RESEND_API_KEY is not configured in Render Environment Variables."
        )

    otp = f"{random.randint(100000, 999999)}"
    print(f"-> RECEIVED /api/auth/send-email-otp for email: {payload.email}, generated otp: {otp}", flush=True)

    headers = {
        "Authorization": f"Bearer {RESEND_API_KEY}",
        "Content-Type": "application/json"
    }
    email_body = {
        "from": RESEND_FROM_EMAIL,
        "to": [payload.email],
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
        req = urllib.request.Request(
            "https://api.resend.com/emails",
            data=json.dumps(email_body).encode("utf-8"),
            headers=headers,
            method="POST"
        )
        with urllib.request.urlopen(req, timeout=15.0) as resp:
            response_text = resp.read().decode("utf-8")
            print(f"Resend API Response: {response_text}", flush=True)
            if resp.status in [200, 201]:
                return {"success": True, "status": "success", "message": "Email OTP sent successfully via Resend"}
            else:
                raise HTTPException(
                    status_code=resp.status,
                    detail=f"Resend error: {response_text}"
                )
    except HTTPException as he:
        raise he
    except Exception as e:
        print(f"Resend error exception: {e}", flush=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to send email via Resend: {str(e)}"
        )
