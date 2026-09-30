import os
import random
from typing import Optional
from fastapi import APIRouter, HTTPException, status
import httpx
from pydantic import BaseModel, EmailStr

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

# API Keys from Render Environment Variables
TWO_FACTOR_API_KEY = os.getenv("TWO_FACTOR_API_KEY", "")
RESEND_API_KEY = os.getenv("RESEND_API_KEY", "")
RESEND_FROM_EMAIL = os.getenv("RESEND_FROM_EMAIL", "onboarding@resend.dev")

# --- Schemas ---

class MobileOTPRequest(BaseModel):
    phone_number: str  # e.g., "+919876543210" or "9876543210"

class MobileOTPVerifyRequest(BaseModel):
    session_id: str
    otp: str

class EmailOTPRequest(BaseModel):
    email: EmailStr
    otp: str

# --- Mobile OTP Endpoints (2Factor.in) ---

@router.post("/send-otp")
@router.post("/send-otp/")
async def send_mobile_otp(payload: MobileOTPRequest):
    """
    Triggers SMS OTP using 2Factor.in AUTOGEN API.
    """
    if not TWO_FACTOR_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="TWO_FACTOR_API_KEY is not configured on server"
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
                    "message": "OTP sent successfully via SMS"
                }
            else:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=data.get("Details", "Failed to send SMS OTP")
                )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"2Factor API error: {str(e)}"
            )

@router.post("/verify-otp")
@router.post("/verify-otp/")
async def verify_mobile_otp(payload: MobileOTPVerifyRequest):
    """
    Verifies SMS OTP using 2Factor.in VERIFY API.
    """
    if not TWO_FACTOR_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="TWO_FACTOR_API_KEY is not configured on server"
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

# --- Email OTP Endpoint (Resend.com) ---

@router.post("/send-email-otp")
@router.post("/send-email-otp/")
async def send_email_otp(payload: EmailOTPRequest):
    """
    Sends Email OTP using Resend API.
    """
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

    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                "https://api.resend.com/emails",
                headers=headers,
                json=email_body,
                timeout=10.0
            )
            if response.status_code in [200, 201]:
                return {"status": "success", "message": "Email OTP sent successfully"}
            else:
                raise HTTPException(
                    status_code=response.status_code,
                    detail=f"Resend error: {response.text}"
                )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Failed to send email: {str(e)}"
            )
