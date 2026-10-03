import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session
from sqlalchemy.pool import StaticPool

from models import AdminMatchAlert, Base, DeviceToken, UserNotification
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
