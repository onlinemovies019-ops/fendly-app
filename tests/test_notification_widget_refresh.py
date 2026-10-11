from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from sqlalchemy.pool import StaticPool

import notifications as notifications_module
from models import AdminMatchAlert, Base, DeviceToken, FoundItem, LostItem, UserNotification
from notifications import send_verified_match_push


def test_user_match_push_is_data_only_for_background_widget_refresh(monkeypatch):
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    sent_messages = []

    class SendResponse:
        success_count = 1

    monkeypatch.setattr(notifications_module, "_firebase_app", lambda: None)
    monkeypatch.setattr(
        notifications_module.messaging,
        "send_each_for_multicast",
        lambda message: sent_messages.append(message) or SendResponse(),
    )

    try:
        with Session(engine) as session:
            session.add_all([
                FoundItem(
                    id="found-1",
                    created_by="user-2",
                    title="Found item",
                    description="Details",
                    lat=0,
                    lng=0,
                ),
                LostItem(
                    id="lost-1",
                    created_by="user-1",
                    title="Lost item",
                    description="Details",
                    lat=0,
                    lng=0,
                ),
                AdminMatchAlert(
                    id="alert-1",
                    found_item_id="found-1",
                    lost_item_id="lost-1",
                    found_title="Found item",
                    lost_title="Lost item",
                    confidence=0.91,
                    reason="Manually verified match",
                    review_status="confirmed",
                    verified_by="admin-1",
                ),
            ])
            session.add(
                DeviceToken(firebase_uid="user-1", token="device-token", platform="android")
            )
            notification = UserNotification(
                firebase_uid="user-1",
                found_item_id="found-1",
                lost_item_id="lost-1",
                match_alert_id="alert-1",
                notification_type="match_verified",
                title="Verified Fendly match",
                body=(
                    "Good news! A potential match for your lost item has been "
                    "verified by the admin. Tap to view instructions."
                ),
                score=0.91,
            )
            session.add(notification)
            session.commit()

            sent = send_verified_match_push(session, notification)

        assert sent == 1
        assert len(sent_messages) == 1
        message = sent_messages[0]
        assert message.notification is None
        assert message.data["title"] == "Verified Fendly match"
        assert message.data["body"] == (
            "Good news! A potential match for your lost item has been "
            "verified by the admin. Tap to view instructions."
        )
        assert message.data["notification_type"] == "match_verified"
        assert message.data["match_alert_id"] == "alert-1"
        assert message.data["lost_item_id"] == "lost-1"
        assert message.android.priority == "high"
    finally:
        engine.dispose()
