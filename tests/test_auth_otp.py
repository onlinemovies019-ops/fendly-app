import pytest
import re
from fastapi import HTTPException
from fastapi.testclient import TestClient
from types import SimpleNamespace
import jwt
from pydantic import ValidationError
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

import auth
import main

from auth import EmailOTPRequest, SendOTPRequest, VerifyEmailOTPRequest, VerifyOTPRequest
from models import Base, EmailOTPChallenge, SmsOTPChallenge, SmsOTPRateLimit, User
from schemas import ProfileUpdate
from routers.users import get_profile, update_profile


@pytest.mark.asyncio
async def test_sms_otp_send_and_verify_use_short_lived_hashed_challenge(monkeypatch, profile_session):
    responses = [{"return": True, "request_id": "request-id-123"}]
    calls = []

    class FakeResponse:
        status_code = 200
        is_success = True

        def raise_for_status(self):
            return None

        def json(self):
            return responses.pop(0)

    class FakeClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, exc_type, exc, traceback):
            return False

        async def post(self, *args, **kwargs):
            calls.append((args, kwargs))
            return FakeResponse()

    monkeypatch.setattr(auth, "FAST2SMS_API_KEY", "test-key")
    monkeypatch.setattr(auth.secrets, "randbelow", lambda upper: 123456)
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    monkeypatch.setattr(auth.httpx, "AsyncClient", FakeClient)
    request = SimpleNamespace(client=SimpleNamespace(host="203.0.113.4"))

    sent = await auth.send_otp(
        SendOTPRequest(mobile="9876543210"),
        request,
        session=profile_session,
    )

    assert sent["success"] is True
    assert calls[0][0] == (auth.FAST2SMS_BULK_URL,)
    assert calls[0][1]["headers"] == {"authorization": "test-key"}
    assert calls[0][1]["data"] == {
        "route": "otp",
        "variables_values": "123456",
        "numbers": "9876543210",
    }
    assert "params" not in calls[0][1]
    challenge = profile_session.query(SmsOTPChallenge).one()
    assert challenge.otp_digest == auth._sms_otp_digest("sms-code", "9876543210:123456")
    assert challenge.phone_digest != "9876543210"
    assert profile_session.query(SmsOTPRateLimit).count() == 3

    verified = await auth.verify_otp(
        VerifyOTPRequest(mobile="9876543210", otp="123456"),
        session=profile_session,
    )
    assert verified["success"] is True
    claims = jwt.decode(
        verified["verification_token"],
        "test-app-secret-0123456789abcdef",
        algorithms=["HS256"],
        audience="fendly-mobile-verification",
        issuer="fendly-api",
    )
    assert claims["sub"] == "9876543210"
    assert claims["scope"] == "mobile_verification"
    assert len(calls) == 1
    assert profile_session.query(SmsOTPChallenge).count() == 0


@pytest.mark.asyncio
async def test_sms_otp_send_exposes_safe_fast2sms_rejection_reason(monkeypatch, profile_session):
    class FakeResponse:
        status_code = 403
        is_success = False

        def json(self):
            return {"return": False, "message": "Insufficient account balance"}

    class FakeClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, exc_type, exc, traceback):
            return False

        async def post(self, *args, **kwargs):
            return FakeResponse()

    monkeypatch.setattr(auth, "FAST2SMS_API_KEY", "private-test-key")
    monkeypatch.setattr(auth.httpx, "AsyncClient", FakeClient)
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    request = SimpleNamespace(client=SimpleNamespace(host="203.0.113.4"))

    with pytest.raises(HTTPException) as exc:
        await auth.send_otp(
            SendOTPRequest(mobile="9876543210"),
            request,
            session=profile_session,
        )

    assert exc.value.status_code == 502
    assert "HTTP 403" in exc.value.detail
    assert "Insufficient account balance" in exc.value.detail
    assert "private-test-key" not in exc.value.detail
    assert profile_session.query(SmsOTPChallenge).count() == 0


def test_sms_otp_send_enforces_persistent_phone_and_ip_limits(monkeypatch, profile_session):
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    now = 1_700_000_000
    phone_digest = auth._sms_otp_digest("phone", "9876543210")
    auth._reserve_sms_otp_quota(
        profile_session,
        phone_digest=phone_digest,
        client_ip="203.0.113.4",
        now=now,
    )

    with pytest.raises(HTTPException) as exc:
        auth._reserve_sms_otp_quota(
            profile_session,
            phone_digest=phone_digest,
            client_ip="203.0.113.4",
            now=now + 1,
        )

    assert exc.value.status_code == 429
    assert profile_session.query(SmsOTPRateLimit).count() == 3


@pytest.mark.asyncio
async def test_sms_otp_send_rejects_invalid_indian_number(monkeypatch, profile_session):
    with pytest.raises(ValidationError):
        SendOTPRequest(mobile="1234567890")
    assert profile_session.query(SmsOTPRateLimit).count() == 0


@pytest.mark.asyncio
async def test_sms_otp_verification_is_bound_to_server_challenge_and_limited(monkeypatch, profile_session):
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    monkeypatch.setattr(auth.time, "time", lambda: 1_700_000_000)
    challenge_id = "challenge-with-guess-limit"
    session_digest = auth._sms_otp_digest("sms-code", f"9876543210:{challenge_id}")
    profile_session.add(
        SmsOTPChallenge(
            otp_digest=session_digest,
            phone_digest=auth._sms_otp_digest("phone", "9876543210"),
            sent_at=1_700_000_000,
            expires_at=1_700_000_600,
            attempts=0,
        )
    )
    profile_session.commit()

    for _ in range(auth.SMS_OTP_MAX_VERIFY_ATTEMPTS):
        result = await auth.verify_otp(
            VerifyOTPRequest(mobile="9876543210", otp="123456"),
            session=profile_session,
        )
        assert result["success"] is False
    assert profile_session.query(SmsOTPChallenge).count() == 0

    with pytest.raises(HTTPException) as exc:
        await auth.verify_otp(
            VerifyOTPRequest(mobile="9876543210", otp="123456"),
            session=profile_session,
        )
    assert exc.value.status_code == 400


def test_policy_and_external_account_deletion_pages_are_publicly_served():
    client = TestClient(main.app)

    policy = client.get("/static/privacy-policy.html")
    deletion = client.get("/static/delete-account.html")

    assert policy.status_code == 200
    assert "account deletion request page" in policy.text
    assert deletion.status_code == 200
    assert "info.fendly@gmail.com" in deletion.text


@pytest.mark.asyncio
async def test_email_otp_requires_mail_configuration(monkeypatch):
    monkeypatch.setattr(auth, "RESEND_API_KEY", "")
    session = _new_profile_session()

    with pytest.raises(HTTPException) as exc:
        await auth.send_email_otp(
            EmailOTPRequest(email="user@example.com"),
            session=session,
            uid="firebase-user",
        )

    assert exc.value.status_code == 503
    assert session.query(EmailOTPChallenge).count() == 0
    session.close()


@pytest.mark.asyncio
async def test_email_otp_is_bound_to_uid_and_verified_before_success(monkeypatch, profile_session):
    class FakeResponse:
        def raise_for_status(self):
            return None

    class FakeClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, exc_type, exc, traceback):
            return False

        async def post(self, *args, **kwargs):
            self.request = kwargs
            return FakeResponse()

    fake_client = FakeClient()
    monkeypatch.setattr(auth, "RESEND_API_KEY", "test-key")
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    monkeypatch.setattr(auth.httpx, "AsyncClient", lambda: fake_client)
    monkeypatch.setattr(auth, "_firebase_app", lambda: object())
    monkeypatch.setattr(auth.firebase_auth, "update_user", lambda *args, **kwargs: None)
    result = await auth.send_email_otp(
        EmailOTPRequest(email="User@example.com"),
        session=profile_session,
        uid="firebase-user",
    )
    code_match = re.search(r">(\d{6})</div>", fake_client.request["json"]["html"])
    assert code_match
    otp = code_match.group(1)

    assert result["success"] is True
    assert "debug_otp" not in result
    assert fake_client.request["json"]["to"] == ["user@example.com"]
    challenge = profile_session.get(EmailOTPChallenge, "firebase-user")
    assert challenge is not None
    assert challenge.code_digest != otp
    assert challenge.sent is True

    with pytest.raises(HTTPException) as exc:
        await auth.send_email_otp(
            EmailOTPRequest(email="other@example.com"),
            session=profile_session,
            uid="firebase-user",
        )
    assert exc.value.status_code == 429

    with pytest.raises(HTTPException) as exc:
        auth.verify_email_otp(
            VerifyEmailOTPRequest(email="user@example.com", otp=otp),
            session=profile_session,
            uid="different-user",
        )
    assert exc.value.status_code == 400

    verified = auth.verify_email_otp(
        VerifyEmailOTPRequest(email="user@example.com", otp=otp),
        session=profile_session,
        uid="firebase-user",
    )
    saved_user = profile_session.query(User).filter_by(firebase_uid="firebase-user").one()
    assert verified["success"] is True
    assert saved_user.email == "user@example.com"
    assert saved_user.email_verified is True
    assert profile_session.get(EmailOTPChallenge, "firebase-user") is None


def test_email_otp_rejects_invalid_code_and_expires(monkeypatch):
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    now = auth.time.time()
    session = _new_profile_session()
    challenge = EmailOTPChallenge(
        firebase_uid="firebase-user",
        email="user@example.com",
        code_digest=auth._email_otp_digest("firebase-user", "user@example.com", "123456"),
        sent_at=int(now),
        expires_at=int(now + 60),
        attempts=0,
        sent=True,
    )
    session.add(challenge)
    session.commit()

    with pytest.raises(HTTPException) as exc:
        auth.verify_email_otp(
            VerifyEmailOTPRequest(email="user@example.com", otp="654321"),
            session=session,
            uid="firebase-user",
        )
    assert exc.value.status_code == 400
    assert session.get(EmailOTPChallenge, "firebase-user").attempts == 1

    monkeypatch.setattr(auth.time, "time", lambda: now + 61)
    with pytest.raises(HTTPException) as exc:
        auth.verify_email_otp(
            VerifyEmailOTPRequest(email="user@example.com", otp="123456"),
            session=session,
            uid="firebase-user",
        )
    assert exc.value.status_code == 400
    assert session.get(EmailOTPChallenge, "firebase-user") is None
    session.close()


@pytest.mark.asyncio
async def test_email_otp_enforces_daily_send_limit(monkeypatch):
    monkeypatch.setattr(auth, "RESEND_API_KEY", "test-key")
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    session = _new_profile_session()
    now = int(auth.time.time())
    session.add(
        EmailOTPChallenge(
            firebase_uid="firebase-user",
            email="user@example.com",
            code_digest=auth._email_otp_digest("firebase-user", "user@example.com", "123456"),
            sent_at=now - 61,
            expires_at=now + 60,
            send_window_started=now - 120,
            send_count=auth.EMAIL_OTP_MAX_SENDS_PER_DAY,
            attempts=0,
            sent=True,
        )
    )
    session.commit()

    with pytest.raises(HTTPException) as exc:
        await auth.send_email_otp(
            EmailOTPRequest(email="user@example.com"),
            session=session,
            uid="firebase-user",
        )

    assert exc.value.status_code == 429
    assert session.get(EmailOTPChallenge, "firebase-user").send_count == 10
    session.close()


def _new_profile_session():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    return sessionmaker(bind=engine, autoflush=False, autocommit=False)()


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


def test_changing_profile_email_clears_verified_status(profile_session):
    profile_session.add(
        User(
            firebase_uid="firebase-email-change",
            email="verified@example.com",
            email_verified=True,
        )
    )
    profile_session.commit()

    update_profile(
        payload=ProfileUpdate(email="new@example.com"),
        session=profile_session,
        uid="firebase-email-change",
    )

    saved = get_profile(session=profile_session, uid="firebase-email-change")
    assert saved["email"] == "new@example.com"
    assert saved["email_verified"] is False


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
