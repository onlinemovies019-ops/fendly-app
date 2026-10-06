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

from auth import (
    ConfirmMobileVerificationRequest,
    CompleteRegistrationRequest,
    EmailOTPRequest,
    PhonePinRecoveryRequest,
    SendOTPRequest,
    SmsPinRecoveryRequest,
    VerifyEmailOTPRequest,
    VerifyOTPRequest,
    confirm_mobile_verification,
    complete_registration,
    recover_pin_with_test_phone,
    recover_pin_with_sms_otp,
)
from models import Base, EmailOTPChallenge, SmsOTPChallenge, SmsOTPRateLimit, User, UsernameReservation
from profile_service import update_profile_record
from schemas import ProfileUpdate, UsernameRequest
from routers.users import (
    check_registration_username,
    get_profile,
    reserve_username,
    update_profile,
)


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
        "route": "q",
        "message": "Your Fendly verification code is 123456",
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


def test_confirm_mobile_verification_persists_only_matching_signed_token(
    monkeypatch, profile_session
):
    uid = "verified-mobile-uid"
    profile_session.add(User(firebase_uid=uid))
    profile_session.commit()
    secret = "test-app-secret-0123456789abcdef"
    monkeypatch.setenv("APP_SECRET_KEY", secret)
    now = int(auth.time.time())
    token = jwt.encode(
        {
            "sub": "9876543210",
            "scope": "mobile_verification",
            "iss": "fendly-api",
            "aud": "fendly-mobile-verification",
            "iat": now,
            "exp": now + 300,
        },
        secret,
        algorithm="HS256",
    )

    result = confirm_mobile_verification(
        ConfirmMobileVerificationRequest(
            mobile="9876543210",
            verification_token=token,
        ),
        session=profile_session,
        uid=uid,
    )

    saved_user = profile_session.query(User).filter_by(firebase_uid=uid).one()
    assert result == {"success": True}
    assert saved_user.mobile == "9876543210"
    assert saved_user.mobile_verified is True


def test_confirm_mobile_verification_rejects_token_for_another_number(
    monkeypatch, profile_session
):
    uid = "verified-mobile-uid"
    user = User(firebase_uid=uid, mobile="9876543210", mobile_verified=False)
    profile_session.add(user)
    profile_session.commit()
    secret = "test-app-secret-0123456789abcdef"
    monkeypatch.setenv("APP_SECRET_KEY", secret)
    now = int(auth.time.time())
    token = jwt.encode(
        {
            "sub": "9876543210",
            "scope": "mobile_verification",
            "iss": "fendly-api",
            "aud": "fendly-mobile-verification",
            "iat": now,
            "exp": now + 300,
        },
        secret,
        algorithm="HS256",
    )

    with pytest.raises(HTTPException) as exc:
        confirm_mobile_verification(
            ConfirmMobileVerificationRequest(
                mobile="9123456789",
                verification_token=token,
            ),
            session=profile_session,
            uid=uid,
        )

    profile_session.refresh(user)
    assert exc.value.status_code == 403
    assert user.mobile == "9876543210"
    assert user.mobile_verified is False


def test_profile_mobile_update_clears_verification_only_when_number_changes(profile_session):
    uid = "verified-mobile-uid"
    user = User(
        firebase_uid=uid,
        mobile="+91 9876543210",
        mobile_verified=True,
    )
    profile_session.add(user)
    profile_session.commit()

    update_profile_record(
        profile_session,
        ProfileUpdate(mobile="9876543210"),
        uid,
    )
    assert user.mobile_verified is True

    update_profile_record(
        profile_session,
        ProfileUpdate(mobile="9123456789"),
        uid,
    )
    assert user.mobile == "9123456789"
    assert user.mobile_verified is False


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


@pytest.mark.asyncio
async def test_sms_otp_send_explains_fast2sms_website_verification_requirement(monkeypatch, profile_session):
    class FakeResponse:
        status_code = 400
        is_success = False

        def json(self):
            return {
                "return": False,
                "message": "Before using OTP Message API, complete website verification. Visit OTP Message menu or use DLT SMS API.",
            }

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
    assert "HTTP 400" in exc.value.detail
    assert "otp message website-verification error" in exc.value.detail.lower()
    assert "Quick SMS" in exc.value.detail
    assert "latest backend deployment" in exc.value.detail
    assert "FAST2SMS_OTP_TEMPLATE_ID" in exc.value.detail
    assert "private-test-key" not in exc.value.detail
    assert profile_session.query(SmsOTPChallenge).count() == 0


@pytest.mark.asyncio
async def test_sms_otp_dlt_rejection_explains_template_configuration(monkeypatch, profile_session):
    class FailedResponse:
        status_code = 400
        is_success = False

        def json(self):
            return {
                "return": False,
                "message": "Before using OTP Message API, complete website verification.",
            }

    class FakeClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, exc_type, exc, traceback):
            return False

        async def post(self, *args, **kwargs):
            return FailedResponse()

    monkeypatch.setattr(auth, "FAST2SMS_API_KEY", "private-test-key")
    monkeypatch.setattr(auth, "FAST2SMS_OTP_TEMPLATE_ID", "registered-template-id")
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
    assert "rejected the configured DLT template" in exc.value.detail
    assert "registered-template-id" not in exc.value.detail
    assert profile_session.query(SmsOTPChallenge).count() == 0


@pytest.mark.asyncio
async def test_sms_otp_send_maps_fast2sms_spam_code_995_to_429(monkeypatch, profile_session):
    class FakeResponse:
        status_code = 400
        is_success = False

        def json(self):
            return {"return": False, "code": 995, "message": "spamming detected"}

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

    assert exc.value.status_code == 429
    assert "wait a moment" in exc.value.detail.lower()
    assert "different phone number" in exc.value.detail.lower()
    assert profile_session.query(SmsOTPChallenge).count() == 0


@pytest.mark.asyncio
async def test_sms_otp_send_maps_fast2sms_request_limits_to_429(monkeypatch, profile_session):
    class FakeResponse:
        status_code = 400
        is_success = False

        def json(self):
            return {"return": False, "message": "Request limits exceeded"}

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

    assert exc.value.status_code == 429
    assert "wait a moment" in exc.value.detail.lower()
    assert "different phone number" in exc.value.detail.lower()
    assert profile_session.query(SmsOTPChallenge).count() == 0


def test_registration_username_availability_checks_existing_profiles_and_reservations(
    monkeypatch, profile_session
):
    monkeypatch.setattr("routers.users._username_exists_in_firebase_auth", lambda _: False)
    monkeypatch.setattr("routers.users._firebase_uid_exists", lambda _: True)
    profile_session.add(User(firebase_uid="existing-profile-uid", username="TakenUser"))
    profile_session.add(
        UsernameReservation(username="reserved_user", firebase_uid="reserved-user-uid")
    )
    profile_session.commit()

    assert check_registration_username("takenuser", session=profile_session)["available"] is False
    assert check_registration_username("RESERVED_USER", session=profile_session)["available"] is False
    assert check_registration_username("new_user", session=profile_session)["available"] is True


def test_registration_releases_username_claims_for_deleted_firebase_users(
    monkeypatch, profile_session
):
    monkeypatch.setattr("routers.users._username_exists_in_firebase_auth", lambda _: False)
    monkeypatch.setattr("routers.users._firebase_uid_exists", lambda _: False)
    old_uid = "deleted-firebase-user"
    profile = User(firebase_uid=old_uid, username="TakenUser")
    reservation = UsernameReservation(username="takenuser", firebase_uid=old_uid)
    profile_session.add_all([profile, reservation])
    profile_session.commit()

    result = check_registration_username("takenuser", session=profile_session)

    assert result == {"username": "takenuser", "available": True}
    assert profile.username is None
    assert profile_session.get(UsernameReservation, "takenuser") is None

    reserved = reserve_username(
        UsernameRequest(username="TakenUser"),
        session=profile_session,
        uid="new-firebase-user",
    )
    assert reserved == {"username": "takenuser", "status": "reserved"}
    assert profile_session.get(UsernameReservation, "takenuser").firebase_uid == "new-firebase-user"


def test_registration_username_availability_checks_firebase_auth_records(
    monkeypatch, profile_session
):
    monkeypatch.setattr(
        "routers.users._username_exists_in_firebase_auth",
        lambda username: username == "firebase_user",
    )

    result = check_registration_username("Firebase_User", session=profile_session)

    assert result == {"username": "firebase_user", "available": False}


def test_complete_registration_sets_password_for_reserved_incomplete_account(
    monkeypatch, profile_session
):
    uid = "registration-uid"
    profile_session.add_all(
        [
            UsernameReservation(username="new_person", firebase_uid=uid),
            User(firebase_uid=uid),
        ]
    )
    profile_session.commit()
    updated = []
    app = object()
    monkeypatch.setattr(auth, "_firebase_app", lambda: app)
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user",
        lambda target_uid, app: SimpleNamespace(email="new_person@login.fendly.app"),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user_by_email",
        lambda email, app: SimpleNamespace(uid=uid),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "update_user",
        lambda target_uid, email, password, app: updated.append(
            (target_uid, email, password, app)
        ),
    )

    result = complete_registration(
        CompleteRegistrationRequest(username="New_Person", pin="1234"),
        session=profile_session,
        uid=uid,
    )

    assert result == {"success": True}
    assert updated == [
        (uid, "new_person@login.fendly.app", "Fendly!new_person#1234", app)
    ]


def test_complete_registration_repairs_missing_username_reservation(
    monkeypatch, profile_session
):
    uid = "registration-uid"
    profile_session.add(User(firebase_uid=uid))
    profile_session.commit()
    app = object()
    updates = []
    monkeypatch.setattr(auth, "_firebase_app", lambda: app)
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user",
        lambda target_uid, app: SimpleNamespace(email="old_name@login.fendly.app"),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user_by_email",
        lambda email, app: SimpleNamespace(uid=uid),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "update_user",
        lambda target_uid, email, password, app: updates.append(
            (target_uid, email, password)
        ),
    )

    result = complete_registration(
        CompleteRegistrationRequest(username="new_person", pin="1234"),
        session=profile_session,
        uid=uid,
    )

    assert result == {"success": True}
    assert profile_session.get(UsernameReservation, "new_person").firebase_uid == uid
    assert updates == [
        (uid, "new_person@login.fendly.app", "Fendly!new_person#1234")
    ]


def test_complete_registration_rejects_reservation_owned_by_another_active_user(
    monkeypatch, profile_session
):
    uid = "registration-uid"
    profile_session.add_all(
        [
            UsernameReservation(username="new_person", firebase_uid="other-active-uid"),
            User(firebase_uid=uid),
        ]
    )
    profile_session.commit()
    app = object()
    monkeypatch.setattr(auth, "_firebase_app", lambda: app)
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user",
        lambda target_uid, app: SimpleNamespace(email=f"{target_uid}@example.test"),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user_by_email",
        lambda email, app: SimpleNamespace(uid=uid),
    )

    with pytest.raises(HTTPException) as exc:
        complete_registration(
            CompleteRegistrationRequest(username="new_person", pin="1234"),
            session=profile_session,
            uid=uid,
        )

    assert exc.value.status_code == 409
    assert "another active account" in exc.value.detail


def test_complete_registration_retry_updates_pin_for_same_completed_account(
    monkeypatch, profile_session
):
    uid = "completed-registration-uid"
    profile_session.add_all(
        [
            UsernameReservation(username="active_person", firebase_uid=uid),
            User(firebase_uid=uid, username="active_person"),
        ]
    )
    profile_session.commit()
    app = object()
    updated = []
    monkeypatch.setattr(auth, "_firebase_app", lambda: app)
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user",
        lambda target_uid, app: SimpleNamespace(email="active_person@login.fendly.app"),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user_by_email",
        lambda email, app: SimpleNamespace(uid=uid),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "update_user",
        lambda *args, **kwargs: updated.append((args, kwargs)),
    )

    result = complete_registration(
        CompleteRegistrationRequest(username="active_person", pin="1234"),
        session=profile_session,
        uid=uid,
    )

    assert result == {"success": True, "already_completed": True}
    assert updated == [
        (
            (uid,),
            {
                "email": "active_person@login.fendly.app",
                "password": "Fendly!active_person#1234",
                "app": app,
            },
        )
    ]


def test_complete_registration_rejects_completed_profile_with_different_username(
    monkeypatch, profile_session
):
    uid = "completed-registration-uid"
    profile_session.add(User(firebase_uid=uid, username="active_person"))
    profile_session.commit()
    app = object()
    monkeypatch.setattr(auth, "_firebase_app", lambda: app)
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user",
        lambda target_uid, app: SimpleNamespace(email="other_name@login.fendly.app"),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user_by_email",
        lambda email, app: SimpleNamespace(uid=uid),
    )

    with pytest.raises(HTTPException) as exc:
        complete_registration(
            CompleteRegistrationRequest(username="other_name", pin="1234"),
            session=profile_session,
            uid=uid,
        )

    assert exc.value.status_code == 409
    assert exc.value.detail == "This account has already completed registration"


def test_complete_registration_requires_matching_firebase_email_for_completed_profile(
    monkeypatch, profile_session
):
    uid = "completed-registration-uid"
    profile_session.add(User(firebase_uid=uid, username="active_person"))
    profile_session.commit()
    app = object()
    monkeypatch.setattr(auth, "_firebase_app", lambda: app)
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user",
        lambda target_uid, app: SimpleNamespace(email="temporary@login.fendly.app"),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user_by_email",
        lambda email, app: SimpleNamespace(uid=uid),
    )

    with pytest.raises(HTTPException) as exc:
        complete_registration(
            CompleteRegistrationRequest(username="active_person", pin="1234"),
            session=profile_session,
            uid=uid,
        )

    assert exc.value.status_code == 409
    assert "sign in with your existing username and PIN" in exc.value.detail


def test_test_phone_recovery_updates_only_matching_verified_account(
    monkeypatch, profile_session
):
    uid = "recovery-account-uid"
    profile_session.add(
        User(
            firebase_uid=uid,
            username="active_person",
            mobile="+91 8657111989",
            mobile_verified=True,
        )
    )
    profile_session.commit()
    app = object()
    updates = []
    monkeypatch.setattr(auth, "_firebase_app", lambda: app)
    monkeypatch.setattr(
        auth.firebase_auth,
        "verify_id_token",
        lambda token, app, check_revoked: {
            "uid": "phone-test-uid",
            "phone_number": "+918657111989",
            "auth_time": int(auth.time.time()),
        },
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user",
        lambda target_uid, app: SimpleNamespace(
            email="active_person@login.fendly.app"
        ),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "update_user",
        lambda *args, **kwargs: updates.append((args, kwargs)),
    )

    result = recover_pin_with_test_phone(
        PhonePinRecoveryRequest(
            username="Active_Person",
            mobile="8657111989",
            pin="1234",
        ),
        credentials=SimpleNamespace(credentials="verified-phone-token"),
        session=profile_session,
    )

    assert result == {"success": True}
    assert updates == [
        (
            (uid,),
            {
                "password": "Fendly!active_person#1234",
                "app": app,
            },
        )
    ]


def test_test_phone_recovery_rejects_phone_token_for_another_number(
    monkeypatch, profile_session
):
    profile_session.add(
        User(
            firebase_uid="recovery-account-uid",
            username="active_person",
            mobile="8657111989",
            mobile_verified=True,
        )
    )
    profile_session.commit()
    updates = []
    monkeypatch.setattr(auth, "_firebase_app", lambda: object())
    monkeypatch.setattr(
        auth.firebase_auth,
        "verify_id_token",
        lambda token, app, check_revoked: {
            "uid": "other-phone-uid",
            "phone_number": "+919876543210",
        },
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "update_user",
        lambda *args, **kwargs: updates.append((args, kwargs)),
    )

    with pytest.raises(HTTPException) as exc:
        recover_pin_with_test_phone(
            PhonePinRecoveryRequest(
                username="active_person",
                mobile="8657111989",
                pin="1234",
            ),
            credentials=SimpleNamespace(credentials="other-phone-token"),
            session=profile_session,
        )

    assert exc.value.status_code == 403
    assert updates == []


def test_sms_phone_recovery_updates_firebase_credentials_after_verified_otp(
    monkeypatch, profile_session
):
    uid = "sms-recovery-account-uid"
    profile_session.add(
        User(
            firebase_uid=uid,
            username="active_person",
            mobile="8657111989",
            mobile_verified=True,
        )
    )
    profile_session.commit()
    app = object()
    updates = []
    secret = "test-app-secret-0123456789abcdef"
    monkeypatch.setenv("APP_SECRET_KEY", secret)
    monkeypatch.setattr(auth, "_firebase_app", lambda: app)
    monkeypatch.setattr(
        auth.firebase_auth,
        "get_user",
        lambda target_uid, app: SimpleNamespace(
            email="active_person@login.fendly.app"
        ),
    )
    monkeypatch.setattr(
        auth.firebase_auth,
        "update_user",
        lambda *args, **kwargs: updates.append((args, kwargs)),
    )
    verification_token = jwt.encode(
        {
            "sub": "8657111989",
            "scope": "mobile_verification",
            "iss": "fendly-api",
            "aud": "fendly-mobile-verification",
            "iat": int(auth.time.time()),
            "exp": int(auth.time.time()) + 600,
            "jti": "verified-otp-token",
        },
        secret,
        algorithm="HS256",
    )

    result = recover_pin_with_sms_otp(
        SmsPinRecoveryRequest(
            username="Active_Person",
            mobile="8657111989",
            pin="5678",
        ),
        credentials=SimpleNamespace(credentials=verification_token),
        session=profile_session,
    )

    assert result == {"success": True}
    assert updates == [
        (
            (uid,),
            {
                "password": "Fendly!active_person#5678",
                "app": app,
            },
        )
    ]


@pytest.mark.asyncio
async def test_sms_otp_send_uses_dlt_when_template_id_set(monkeypatch, profile_session):
    responses = [{"return": True, "request_id": "request-id-456"}]
    calls = []

    class FakeResponse:
        status_code = 200
        is_success = True

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
    monkeypatch.setattr(auth, "FAST2SMS_OTP_TEMPLATE_ID", "1207161234567890123")
    monkeypatch.setattr(auth.secrets, "randbelow", lambda upper: 654321)
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    monkeypatch.setattr(auth.httpx, "AsyncClient", FakeClient)
    request = SimpleNamespace(client=SimpleNamespace(host="203.0.113.4"))

    sent = await auth.send_otp(
        SendOTPRequest(mobile="9876543210"),
        request,
        session=profile_session,
    )

    assert sent["success"] is True
    assert calls[0][1]["data"] == {
        "route": "dlt",
        "message": "1207161234567890123",
        "variables_values": "654321",
        "numbers": "9876543210",
    }


@pytest.mark.asyncio
async def test_sms_otp_failed_provider_does_not_consume_quota(monkeypatch, profile_session):
    class FailedResponse:
        status_code = 400
        is_success = False

        def json(self):
            return {"return": False, "message": "Fast2SMS rejected request"}

    class SuccessResponse:
        status_code = 200
        is_success = True

        def json(self):
            return {"return": True, "request_id": "ok-123"}

    current_response = FailedResponse()

    class FakeClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, exc_type, exc, traceback):
            return False

        async def post(self, *args, **kwargs):
            return current_response

    monkeypatch.setattr(auth, "FAST2SMS_API_KEY", "test-key")
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    monkeypatch.setattr(auth.httpx, "AsyncClient", FakeClient)
    request = SimpleNamespace(client=SimpleNamespace(host="203.0.113.4"))

    # Fail 5 times due to provider error
    for _ in range(5):
        with pytest.raises(HTTPException) as exc:
            await auth.send_otp(
                SendOTPRequest(mobile="9876543210"),
                request,
                session=profile_session,
            )
        assert exc.value.status_code == 502

    # Now provider succeeds — should NOT be blocked by 429 because previous failed attempts created no challenge
    current_response = SuccessResponse()
    sent = await auth.send_otp(
        SendOTPRequest(mobile="9876543210"),
        request,
        session=profile_session,
    )
    assert sent["success"] is True


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


def test_email_otp_conflict_explains_safe_resolution_and_preserves_challenge(
    monkeypatch, profile_session
):
    monkeypatch.setenv("APP_SECRET_KEY", "test-app-secret-0123456789abcdef")
    uid = "firebase-user"
    email = "user@example.com"
    now = int(auth.time.time())
    challenge = EmailOTPChallenge(
        firebase_uid=uid,
        email=email,
        code_digest=auth._email_otp_digest(uid, email, "123456"),
        sent_at=now,
        expires_at=now + 60,
        attempts=0,
        sent=True,
    )
    profile_session.add(challenge)
    profile_session.commit()
    monkeypatch.setattr(auth, "_firebase_app", lambda: object())

    def email_in_use(*args, **kwargs):
        raise auth.firebase_auth.EmailAlreadyExistsError(
            "email already exists", None, None
        )

    monkeypatch.setattr(auth.firebase_auth, "update_user", email_in_use)

    with pytest.raises(HTTPException) as exc:
        auth.verify_email_otp(
            VerifyEmailOTPRequest(email=email, otp="123456"),
            session=profile_session,
            uid=uid,
        )

    assert exc.value.status_code == 409
    assert "sign in to the account currently using it" in exc.value.detail
    assert "Do not delete the account" in exc.value.detail
    assert profile_session.get(EmailOTPChallenge, uid) is not None


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
