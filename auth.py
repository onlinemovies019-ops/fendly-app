import os
from fastapi import APIRouter, HTTPException, status
import httpx
from pydantic import BaseModel

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

TWO_FACTOR_API_KEY = os.getenv("TWO_FACTOR_API_KEY", "")

class SendOTPRequest(BaseModel):
    phone_number: str

class VerifyOTPRequest(BaseModel):
    session_id: str
    otp: str

@router.post("/send-otp")
async def send_otp(payload: SendOTPRequest):
    if not TWO_FACTOR_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="TWO_FACTOR_API_KEY is not configured in Render Environment Variables"
        )

    clean_phone = payload.phone_number.replace(" ", "").replace("-", "")
    url = f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/{clean_phone}/AUTOGEN"

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, timeout=10.0)
            data = response.json()
            if data.get("Status") == "Success":
                return {
                    "status": "success",
                    "session_id": data.get("Details"),
                    "message": "OTP sent successfully"
                }
            else:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=data.get("Details", "Failed to send OTP")
                )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"SMS error: {str(e)}"
            )

@router.post("/verify-otp")
async def verify_otp(payload: VerifyOTPRequest):
    if not TWO_FACTOR_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="TWO_FACTOR_API_KEY is not configured in Render Environment Variables"
        )

    url = f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/VERIFY/{payload.session_id}/{payload.otp}"

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, timeout=10.0)
            data = response.json()
            if data.get("Status") == "Success" and data.get("Details") == "OTP Matched":
                return {"status": "success", "message": "OTP verified successfully"}
            else:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Invalid or expired OTP"
                )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Verification failed: {str(e)}"
            )