import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session
from sqlalchemy.pool import StaticPool

from models import AdminMatchAlert, Base, DeviceToken, User, UserNotification
from notifications import send_match_notifications
from routers import admin as admin_module
from routers import users as users_module


@pytest.fixture
def notification_session():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    with Session(engine) as session:
        yield session
    engine.dispose()


def test_user_match_ping_is_persisted_without_device_token(notification_session):
    sent = send_match_notifications(notification_session, {"user-1"}, "found-1", 0.91)

    notifications = notification_session.scalars(select(UserNotification)).all()
    assert sent == 0
    assert len(notifications) == 1
    assert notifications[0].firebase_uid == "user-1"
    assert notifications[0].found_item_id == "found-1"
    assert notifications[0].is_read is False


def test_user_inbox_and_read_state_are_scoped_to_authenticated_uid(notification_session):
    notification_session.add_all([
        UserNotification(
            firebase_uid="user-1",
            found_item_id="found-1",
            title="Possible Fendly match",
            body="A found item matches yours (91% match).",
            score=0.91,
        ),
        UserNotification(
            firebase_uid="user-2",
            found_item_id="found-2",
            title="Possible Fendly match",
            body="A found item matches yours (83% match).",
            score=0.83,
        ),
    ])
    notification_session.commit()

    result = users_module.get_user_notifications(session=notification_session, uid="user-1")
    assert result["unread_count"] == 1
    assert len(result["notifications"]) == 1
    assert result["notifications"][0]["found_item_id"] == "found-1"

    users_module.mark_user_notifications_read(session=notification_session, uid="user-1")
    own, other = notification_session.scalars(
        select(UserNotification).order_by(UserNotification.firebase_uid)
    ).all()
    assert own.is_read is True
    assert other.is_read is False


def test_admin_auth_allows_allowlisted_firebase_uid(notification_session, monkeypatch):
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "admin-uid")

    assert admin_module.require_admin(uid="admin-uid", session=notification_session) == "admin-uid"


def test_admin_auth_allows_only_profile_with_verified_configured_admin_email(
    notification_session, monkeypatch
):
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "different-admin-uid")
    monkeypatch.setenv("ADMIN_EMAIL", "Info.Fendly@gmail.com")
    notification_session.add_all([
        User(
            firebase_uid="matching-email-uid",
            email="info.fendly@gmail.com",
            email_verified=True,
        ),
        User(
            firebase_uid="unverified-email-uid",
            email="info.fendly@gmail.com",
            email_verified=False,
        ),
        User(
            firebase_uid="different-email-uid",
            email="another@example.com",
            email_verified=True,
        ),
    ])
    notification_session.commit()

    assert admin_module.require_admin(
        uid="matching-email-uid", session=notification_session
    ) == "matching-email-uid"
    for uid in ("unverified-email-uid", "different-email-uid"):
        with pytest.raises(admin_module.HTTPException) as error:
            admin_module.require_admin(uid=uid, session=notification_session)
        assert error.value.status_code == 403
        if uid == "unverified-email-uid":
            assert "verify its email by OTP" in error.value.detail
        else:
            assert "does not match the configured admin email" in error.value.detail


def test_admin_auth_requires_some_admin_configuration(notification_session, monkeypatch):
    monkeypatch.delenv("ADMIN_FIREBASE_UIDS", raising=False)
    monkeypatch.delenv("ADMIN_EMAIL", raising=False)

    with pytest.raises(admin_module.HTTPException) as error:
        admin_module.require_admin(uid="user-uid", session=notification_session)

    assert error.value.status_code == 503


def test_admin_auth_explains_uid_allowlist_mismatch(
    notification_session, monkeypatch
):
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "configured-admin-uid")
    monkeypatch.setenv("ADMIN_EMAIL", "info.fendly@gmail.com")
    notification_session.add(
        User(
            firebase_uid="signed-in-uid",
            email="another@example.com",
            email_verified=True,
        )
    )
    notification_session.commit()

    with pytest.raises(admin_module.HTTPException) as error:
        admin_module.require_admin(uid="signed-in-uid", session=notification_session)

    assert error.value.status_code == 403
    assert "Firebase UID 'signed-in-uid'" in error.value.detail
    assert "UID allowlisted=False" in error.value.detail
    assert "profile email verified=True" in error.value.detail
    assert "profile email matches ADMIN_EMAIL=False" in error.value.detail


@pytest.mark.asyncio
async def test_admin_alert_list_does_not_include_user_inbox(notification_session, monkeypatch):
    notification_session.add_all([
        AdminMatchAlert(
            id="admin-alert-1",
            found_item_id="found-1",
            lost_item_id="lost-1",
            found_title="Found wallet",
            lost_title="Lost wallet",
            confidence=0.91,
            reason="Possible match",
        ),
        UserNotification(
            firebase_uid="user-1",
            found_item_id="found-1",
            title="Possible Fendly match",
            body="A found item matches yours (91% match).",
            score=0.91,
        ),
    ])
    notification_session.commit()
    monkeypatch.setattr(admin_module, "_supabase_admin_alert_request", lambda *args, **kwargs: None)

    result = await admin_module.list_match_alerts(session=notification_session, _="admin-uid")

    assert len(result) == 1
    assert result[0]["id"] == "admin-alert-1"


@pytest.mark.asyncio
async def test_admin_alert_list_falls_back_to_database_when_supabase_is_unavailable(
    notification_session, monkeypatch, caplog
):
    alert = AdminMatchAlert(
        id="admin-alert-1",
        found_item_id="found-1",
        lost_item_id="lost-1",
        found_title="Found wallet",
        lost_title="Lost wallet",
        confidence=0.91,
        reason="Possible match",
    )
    notification_session.add(alert)
    notification_session.commit()

    def fail_supabase(*args, **kwargs):
        raise admin_module.HTTPException(503, "Supabase unavailable")

    monkeypatch.setattr(admin_module, "_supabase_admin_alert_request", fail_supabase)

    result = await admin_module.list_match_alerts(session=notification_session, _="admin-uid")

    assert [item["id"] for item in result] == ["admin-alert-1"]
    assert "falling back to the application database" in caplog.text
