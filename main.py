import os
import json
import urllib.request
import urllib.parse
from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, EmailStr
from sqlalchemy import select, text
from sqlalchemy.orm import Session
from auth import router as auth_router
from database import engine, get_db
from models import Base, User
from routers.items import router as items_router
from routers.notifications import router as notifications_router
from routers.users import router as users_router
from fastapi import FastAPI
from routers.admin import router as admin_router
from routers.payments import router as payments_router


def _validate_production_config() -> None:
    if os.getenv("ENVIRONMENT", "development").lower() != "production":
        return

    required = (
        "DATABASE_URL",
        "FIREBASE_SERVICE_ACCOUNT_JSON",
        "APP_SECRET_KEY",
        "SMTP_HOST",
        "SMTP_USERNAME",
        "SMTP_PASSWORD",
        "SMTP_FROM_EMAIL",
    )
    missing = [name for name in required if not os.getenv(name)]
    if missing:
        raise RuntimeError(f"Missing production configuration: {', '.join(missing)}")


@asynccontextmanager
async def lifespan(_: FastAPI):
    environment = os.getenv("ENVIRONMENT", "development").lower()
    if environment == "production":
        _validate_production_config()
    if engine is not None:
        with engine.begin() as connection:
            dialect_name = connection.dialect.name
            Base.metadata.create_all(bind=connection)
            if dialect_name == "postgresql":
                connection.execute(text("CREATE EXTENSION IF NOT EXISTS vector"))

            def table_has_column(table_name: str, column_name: str) -> bool:
                try:
                    if dialect_name == "postgresql":
                        table = connection.execute(
                            text(f"SELECT column_name FROM information_schema.columns WHERE table_name = '{table_name}'")
                        ).fetchall()
                        return any(row[0] == column_name for row in table)
                    columns = connection.execute(text(f"PRAGMA table_info({table_name})")).fetchall()
                    return any(row[1] == column_name for row in columns)
                except Exception:
                    return False

            def table_exists(conn, table_name: str) -> bool:
                if dialect_name == "postgresql":
                    result = conn.execute(text(f"SELECT to_regclass('{table_name}') IS NOT NULL AS exists")).fetchone()
                    return bool(result and result[0])
                result = conn.execute(
                    text(f"SELECT name FROM sqlite_master WHERE type='table' AND name='{table_name}'")
                ).fetchone()
                return bool(result)

            def add_column_if_missing(
                table_name: str,
                column_name: str,
                column_type: str,
                *,
                not_null: bool = False,
                default: str | None = None,
            ):
                if table_has_column(table_name, column_name):
                    return
                if not table_exists(connection, table_name):
                    return
                definition = column_type
                if not_null:
                    definition += " NOT NULL"
                if default is not None:
                    definition += f" DEFAULT {default}"
                if dialect_name == "postgresql":
                    connection.execute(text(f"ALTER TABLE {table_name} ADD COLUMN IF NOT EXISTS {column_name} {definition}"))
                else:
                    if column_type.startswith("vector"):
                        return
                    connection.execute(text(f"ALTER TABLE {table_name} ADD COLUMN {column_name} {definition}"))

            # --- USER TABLE COLUMNS (AUTOMATIC SUPABASE MIGRATION) ---
            add_column_if_missing("users", "firebase_uid", "varchar(255)")
            add_column_if_missing("users", "first_name", "varchar(120)")
            add_column_if_missing("users", "surname", "varchar(120)")
            add_column_if_missing("users", "username", "varchar(32)")
            add_column_if_missing("users", "full_name", "varchar(160)")
            add_column_if_missing("users", "email", "varchar(320)")
            add_column_if_missing("users", "mobile", "varchar(32)")
            add_column_if_missing("users", "state", "varchar(120)")
            add_column_if_missing("users", "city", "varchar(120)")
            add_column_if_missing("users", "profile_photo_url", "varchar(1000)")
            add_column_if_missing("users", "email_verified", "boolean", not_null=True, default="false")
            add_column_if_missing("users", "mobile_verified", "boolean", not_null=True, default="false")

            # --- ITEM EMBEDDINGS & REPORT COLUMNS ---
            add_column_if_missing("lost_items", "image_embedding", "vector(512)")
            add_column_if_missing("found_items", "image_embedding", "vector(512)")
            add_column_if_missing("lost_items", "embedding", "vector(1536)")
            add_column_if_missing("found_items", "embedding", "vector(1536)")
            add_column_if_missing("lost_items", "report_date", "varchar(32)")
            add_column_if_missing("lost_items", "report_location", "varchar(500)")
            add_column_if_missing("lost_items", "edit_count", "integer", not_null=True, default="0")
            add_column_if_missing("found_items", "report_date", "varchar(32)")
            add_column_if_missing("found_items", "report_location", "varchar(500)")
            add_column_if_missing("found_items", "edit_count", "integer", not_null=True, default="0")
    yield


app = FastAPI(title="Fendly API", version="1.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=os.getenv("CORS_ORIGINS", "*").split(","),
    allow_methods=["*"],
    allow_headers=["*"],
)

app.mount("/static", StaticFiles(directory="static"), name="static")

# --- ROUTER INCLUSIONS ---
app.include_router(auth_router)
app.include_router(items_router)
app.include_router(notifications_router)
app.include_router(users_router)
app.include_router(admin_router)
app.include_router(payments_router)


TWO_FACTOR_API_KEY = os.getenv("TWO_FACTOR_API_KEY", "")
RESEND_API_KEY = os.getenv("RESEND_API_KEY", "")
RESEND_FROM_EMAIL = os.getenv("RESEND_FROM_EMAIL", "onboarding@resend.dev")

class MobileOTPRequest(BaseModel):
    phone_number: str

class MobileOTPVerifyRequest(BaseModel):
    session_id: str
    otp: str

class EmailOTPRequest(BaseModel):
    email: EmailStr
    otp: str

@app.post("/api/auth/send-otp")
@app.post("/api/auth/send-otp/")
async def send_mobile_otp_direct(payload: MobileOTPRequest):
    if not TWO_FACTOR_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="TWO_FACTOR_API_KEY is not configured on server"
        )
    clean_phone = payload.phone_number.replace(" ", "").replace("-", "")
    url = f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/{clean_phone}/AUTOGEN"
    try:
        req = urllib.request.Request(url, method="GET")
        with urllib.request.urlopen(req, timeout=10.0) as resp:
            data = json.loads(resp.read().decode("utf-8"))
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

@app.post("/api/auth/verify-otp")
@app.post("/api/auth/verify-otp/")
async def verify_mobile_otp_direct(payload: MobileOTPVerifyRequest):
    if not TWO_FACTOR_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="TWO_FACTOR_API_KEY is not configured on server"
        )
    url = f"https://2factor.in/API/V1/{TWO_FACTOR_API_KEY}/SMS/VERIFY/{payload.session_id}/{payload.otp}"
    try:
        req = urllib.request.Request(url, method="GET")
        with urllib.request.urlopen(req, timeout=10.0) as resp:
            data = json.loads(resp.read().decode("utf-8"))
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

@app.post("/api/auth/send-email-otp")
@app.post("/api/auth/send-email-otp/")
async def send_email_otp_direct(payload: EmailOTPRequest):
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


app = FastAPI()

# Do NOT add prefix="/api/auth" here if it is already defined in auth.py
app.include_router(auth_router)

@app.get("/health")
def health_check():
    return {"status": "ok"}


@app.get("/user/me")
def get_user_me_root(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
):
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None:
        user = User(firebase_uid=uid, email_verified=False, mobile_verified=False)
        session.add(user)
        try:
            session.commit()
            session.refresh(user)
        except Exception:
            session.rollback()
    is_verified = bool(user.email_verified)
    return {
        "is_verified": is_verified,
        "email_verified": user.email_verified,
        "mobile_verified": user.mobile_verified,
        "email": user.email,
        "mobile": user.mobile,
    }
