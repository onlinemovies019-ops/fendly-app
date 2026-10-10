from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from sqlalchemy.pool import StaticPool

import notifications as notifications_module
from models import Base, DeviceToken
from notifications import send_match_notifications


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
            session.add(
                DeviceToken(firebase_uid="user-1", token="device-token", platform="android")
            )
            session.commit()

            sent = send_match_notifications(session, {"user-1"}, "found-1", 0.91)

        assert sent == 1
        assert len(sent_messages) == 1
        message = sent_messages[0]
        assert message.notification is None
        assert message.data["title"] == "Possible Fendly match"
        assert message.data["body"] == "A found item matches yours (91% match)."
        assert message.android.priority == "high"
    finally:
        engine.dispose()
