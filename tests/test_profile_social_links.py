from datetime import datetime, timezone

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from models import Base, User
from profile_service import update_profile_record
from routers.beacon import BeaconSyncRequest, sync_beacon_events
from schemas import ProfileUpdate


def _session_factory():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    return sessionmaker(bind=engine, autoflush=False, autocommit=False)


def test_update_profile_record_persists_social_urls():
    session_factory = _session_factory()
    with session_factory() as session:
        user = User(firebase_uid="profile-social-user")
        session.add(user)
        session.commit()

        update_profile_record(
            session,
            ProfileUpdate(
                instagram_url="https://www.instagram.com/fendly_community/",
                facebook_url="https://www.facebook.com/fendly.community/",
                x_url="https://x.com/fendly",
            ),
            uid="profile-social-user",
        )

        saved = session.get(User, user.id)
        assert saved.instagram_url == "https://www.instagram.com/fendly_community/"
        assert saved.facebook_url == "https://www.facebook.com/fendly.community/"
        assert saved.x_url == "https://x.com/fendly"


def test_sync_beacon_events_deduplicates_by_event_id():
    session_factory = _session_factory()
    with session_factory() as session:
        payload = BeaconSyncRequest(
            events=[
                {
                    "event_id": "beacon-1",
                    "latitude": 12.34,
                    "longitude": 56.78,
                    "timestamp": datetime(2025, 1, 1, 9, 0, tzinfo=timezone.utc),
                    "source": "device",
                },
                {
                    "event_id": "beacon-1",
                    "latitude": 12.34,
                    "longitude": 56.78,
                    "timestamp": datetime(2025, 1, 1, 9, 0, tzinfo=timezone.utc),
                    "source": "device",
                },
            ]
        )
        result = sync_beacon_events(payload, session=session, uid="beacon-user")

    assert result["received"] == 2
    assert result["inserted"] == 1
    assert result["duplicates"] == 1
