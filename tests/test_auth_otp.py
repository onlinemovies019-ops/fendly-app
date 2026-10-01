import pytest
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

import main

from auth import (
    SendEmailOtpRequest,
    VerifyEmailOtpRequest,
    _EMAIL_OTP_STORE,
    create_session_token,
    send_email_otp,
    verify_email_otp,
    verify_session_token,
)
from models import Base, User
from schemas import ProfileUpdate
from routers.users import get_profile, update_profile


def test_session_token_round_trip():
    token = create_session_token("2factor:abc123", {"phone_number": "+919999999999"})
    payload = verify_session_token(token)

    assert payload["sub"] == "2factor:abc123"
    assert payload["phone_number"] == "+919999999999"


@pytest.mark.asyncio
async def test_email_otp_debug_fallback(monkeypatch):
    monkeypatch.delenv("SMTP_HOST", raising=False)
    monkeypatch.delenv("SMTP_USERNAME", raising=False)
    monkeypatch.delenv("SMTP_PASSWORD", raising=False)
    monkeypatch.delenv("EMAIL_OTP_DEBUG_MODE", raising=False)
    monkeypatch.setenv("ENVIRONMENT", "development")
    _EMAIL_OTP_STORE.clear()

    sent = await send_email_otp(SendEmailOtpRequest(email="user@example.com"))
    assert sent["success"] is True
    assert sent.get("debug_otp")

    otp = _EMAIL_OTP_STORE["user@example.com"]["otp"]
    verified = await verify_email_otp(VerifyEmailOtpRequest(email="user@example.com", otp=otp))

    assert verified["success"] is True
    assert "token" in verified


@pytest.fixture
def profile_session():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)
    session = SessionLocal()
    yield session
    session.close()


def test_profile_save_and_fetch_round_trip(profile_session):
    update_profile(
        payload=ProfileUpdate(
            username="demo_user",
            full_name="Demo User",
            email="demo@example.com",
            mobile="9876543210",
            state="Maharashtra",
            city="Mumbai",
        ),
        session=profile_session,
        uid="firebase-user-123",
    )

    saved = get_profile(session=profile_session, uid="firebase-user-123")
    assert saved["username"] == "demo_user"
    assert saved["full_name"] == "Demo User"
    assert saved["email"] == "demo@example.com"
    assert saved["mobile"] == "9876543210"
    assert saved["state"] == "Maharashtra"
    assert saved["city"] == "Mumbai"


def test_same_firebase_uid_profile_is_shared_across_devices(profile_session):
    update_profile(
        payload=ProfileUpdate(
            username="shared_user",
            full_name="Same User",
            email="same@example.com",
            mobile="9876543210",
            state="Maharashtra",
            city="Pune",
        ),
        session=profile_session,
        uid="firebase-user-shared",
    )

    update_profile(
        payload=ProfileUpdate(
            username="shared_user",
            full_name="Updated Same User",
            email="updated@example.com",
            mobile="9876543210",
            state="Maharashtra",
            city="Nagpur",
        ),
        session=profile_session,
        uid="firebase-user-shared",
    )

    saved = get_profile(session=profile_session, uid="firebase-user-shared")
    assert saved["username"] == "shared_user"
    assert saved["full_name"] == "Updated Same User"
    assert saved["email"] == "updated@example.com"
    assert saved["mobile"] == "9876543210"
    assert saved["state"] == "Maharashtra"
    assert saved["city"] == "Nagpur"


def test_partial_profile_update_does_not_clear_existing_data(profile_session):
    update_profile(
        payload=ProfileUpdate(
            username="partial_user",
            full_name="Partial User",
            email="partial@example.com",
            mobile="9876543210",
            state="Maharashtra",
            city="Pune",
        ),
        session=profile_session,
        uid="firebase-partial-user",
    )

    update_profile(
        payload=ProfileUpdate(
            username="partial_user",
            full_name="",
            email="",
            mobile="",
            state="",
            city="",
        ),
        session=profile_session,
        uid="firebase-partial-user",
    )

    saved = get_profile(session=profile_session, uid="firebase-partial-user")
    assert saved["username"] == "partial_user"
    assert saved["full_name"] == "Partial User"
    assert saved["email"] == "partial@example.com"
    assert saved["mobile"] == "9876543210"
    assert saved["state"] == "Maharashtra"
    assert saved["city"] == "Pune"


@pytest.mark.asyncio
async def test_lifespan_migrates_profile_columns_for_existing_users():
    original_engine = main.engine
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    main.engine = engine
    try:
        with engine.begin() as connection:
            connection.execute(
                text(
                    "CREATE TABLE users ("
                    "id VARCHAR(36) PRIMARY KEY, "
                    "firebase_uid VARCHAR(128), "
                    "username VARCHAR(32), "
                    "full_name VARCHAR(160), "
                    "email VARCHAR(320), "
                    "mobile VARCHAR(32), "
                    "created_at DATETIME"
                    ")"
                )
            )
        async with main.lifespan(main.app):
            pass
        with engine.begin() as connection:
            columns = [row[1] for row in connection.execute(text("PRAGMA table_info(users)"))]
        assert "state" in columns
        assert "city" in columns
    finally:
        main.engine = original_engine
