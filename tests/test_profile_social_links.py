from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from models import Base, User
from profile_service import update_profile_record
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
            ),
            uid="profile-social-user",
        )

        saved = session.get(User, user.id)
        assert saved.instagram_url == "https://www.instagram.com/fendly_community/"
        assert saved.facebook_url == "https://www.facebook.com/fendly.community/"
        assert "x_url" not in ProfileUpdate.model_fields
