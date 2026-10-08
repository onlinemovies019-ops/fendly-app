from uuid import uuid4

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from sqlalchemy.pool import StaticPool

import routers.users as users
from models import (
    AdminMatchAlert,
    Base,
    DeviceToken,
    EmailOTPChallenge,
    FoundItem,
    LostItem,
    SocialPublication,
    User,
    UserNotification,
    UsernameReservation,
)


@pytest.fixture
def deletion_session():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    with Session(engine) as session:
        yield session


def test_account_deletion_removes_owned_static_profile_photo(monkeypatch, tmp_path):
    filename = f"{uuid4().hex}.jpg"
    photo = tmp_path / filename
    photo.write_bytes(b"profile image")
    monkeypatch.setenv("PUBLIC_BASE_URL", "https://api.example.test")
    monkeypatch.setenv("UPLOAD_DIR", str(tmp_path))
    monkeypatch.setenv("SUPABASE_URL", "")

    users._delete_profile_photo_asset(f"https://api.example.test/static/uploads/{filename}")

    assert not photo.exists()


def test_account_deletion_preserves_photo_shared_by_a_report(deletion_session, monkeypatch, tmp_path):
    filename = f"{uuid4().hex}.jpg"
    photo = tmp_path / filename
    photo.write_bytes(b"report image")
    photo_url = f"https://api.example.test/static/uploads/{filename}"
    monkeypatch.setenv("PUBLIC_BASE_URL", "https://api.example.test")
    monkeypatch.setenv("UPLOAD_DIR", str(tmp_path))
    monkeypatch.setenv("SUPABASE_URL", "")
    deletion_session.add(
        LostItem(
            created_by="another-user",
            title="Wallet",
            description="Blue wallet",
            lat=0,
            lng=0,
            image_url=photo_url,
            image_urls=[photo_url],
        )
    )
    deletion_session.commit()

    is_referenced = users._photo_is_still_referenced(deletion_session, photo_url, "deleted-user")
    assert is_referenced
    if not is_referenced:
        users._delete_profile_photo_asset(photo_url)

    assert photo.exists()


def test_account_deletion_preserves_poster_referenced_by_another_report(deletion_session):
    poster_url = "https://api.example.test/static/uploads/00000000000000000000000000000001.jpg"
    deletion_session.add(
        FoundItem(
            created_by="another-user",
            title="Cat",
            description="White cat",
            lat=0,
            lng=0,
            social_poster_url=poster_url,
        )
    )
    deletion_session.commit()

    assert users._photo_is_still_referenced(deletion_session, poster_url, "deleted-user")


def test_account_deletion_removes_owned_supabase_profile_photo(monkeypatch):
    filename = f"{uuid4().hex}.webp"
    requested = {}

    class FakeResponse:
        def raise_for_status(self):
            return None

    def fake_delete(url, headers, timeout):
        requested.update(url=url, headers=headers, timeout=timeout)
        return FakeResponse()

    monkeypatch.setenv("SUPABASE_URL", "https://storage.example.test")
    monkeypatch.setenv("SUPABASE_STORAGE_BUCKET", "uploads")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "test-service-key")
    monkeypatch.setenv("PUBLIC_BASE_URL", "")
    monkeypatch.setattr(users.httpx, "delete", fake_delete)

    users._delete_profile_photo_asset(
        f"https://storage.example.test/storage/v1/object/public/uploads/{filename}"
    )

    assert requested["url"] == f"https://storage.example.test/storage/v1/object/uploads/{filename}"
    assert requested["headers"]["Authorization"] == "Bearer test-service-key"


def test_account_deletion_removes_supabase_visual_index_and_alerts(monkeypatch):
    requests = []

    class FakeResponse:
        def raise_for_status(self):
            return None

    def fake_delete(url, params, headers, timeout):
        requests.append((url, params, headers, timeout))
        return FakeResponse()

    monkeypatch.setenv("SUPABASE_URL", "https://storage.example.test")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "test-service-key")
    monkeypatch.setattr(users.httpx, "delete", fake_delete)

    users._delete_supabase_account_matching_data({"report-1", "report-2"})

    assert [request[0] for request in requests] == [
        "https://storage.example.test/rest/v1/items",
        "https://storage.example.test/rest/v1/admin_match_alerts",
    ]
    assert requests[0][1] == {"source_id": 'in.("report-1","report-2")'}
    assert requests[1][1]["or"] == (
        '(found_item_id.in.("report-1","report-2"),'
        'lost_item_id.in.("report-1","report-2"))'
    )


def test_supabase_matching_cleanup_fails_when_credentials_are_missing(monkeypatch):
    monkeypatch.delenv("SUPABASE_URL", raising=False)
    monkeypatch.delenv("SUPABASE_SERVICE_ROLE_KEY", raising=False)

    with pytest.raises(RuntimeError, match="Supabase cleanup credentials"):
        users._delete_supabase_account_matching_data({"report-1"})


def test_delete_account_checks_supabase_before_deleting_report_data(
    deletion_session,
    monkeypatch,
):
    uid = "firebase-delete-user"
    deletion_session.add(
        LostItem(
            id="lost-1",
            created_by=uid,
            title="Lost bag",
            description="Black bag",
            lat=1.0,
            lng=2.0,
            image_url="https://api.example.test/static/uploads/report.jpg",
        )
    )
    deletion_session.commit()
    monkeypatch.delenv("SUPABASE_URL", raising=False)
    monkeypatch.delenv("SUPABASE_SERVICE_ROLE_KEY", raising=False)
    side_effects = []
    monkeypatch.setattr(
        users,
        "_delete_profile_photo_asset",
        lambda _: side_effects.append("asset"),
    )
    monkeypatch.setattr(
        users,
        "_delete_firestore_account_copies",
        lambda *args: side_effects.append("firestore"),
    )
    monkeypatch.setattr(
        users,
        "_firebase_app",
        lambda: side_effects.append("firebase"),
    )

    with pytest.raises(users.HTTPException) as exc_info:
        users.delete_account(
            response=users.Response(),
            session=deletion_session,
            uid=uid,
        )

    assert exc_info.value.status_code == 500
    assert side_effects == []
    assert deletion_session.get(LostItem, "lost-1") is not None


def test_account_deletion_removes_owned_cloudinary_profile_photo(monkeypatch):
    requested = {}

    class FakeResponse:
        def raise_for_status(self):
            return None

        def json(self):
            return {"result": "ok"}

    def fake_post(url, data, timeout):
        requested.update(url=url, data=data, timeout=timeout)
        return FakeResponse()

    monkeypatch.setenv("SUPABASE_URL", "")
    monkeypatch.setenv("PUBLIC_BASE_URL", "")
    monkeypatch.setenv("CLOUDINARY_CLOUD_NAME", "demo-cloud")
    monkeypatch.setenv("CLOUDINARY_API_KEY", "test-api-key")
    monkeypatch.setenv("CLOUDINARY_API_SECRET", "test-api-secret")
    monkeypatch.setattr(users.time, "time", lambda: 123456)
    monkeypatch.setattr(users.httpx, "post", fake_post)

    users._delete_profile_photo_asset(
        "https://res.cloudinary.com/demo-cloud/image/upload/c_fill,w_200/"
        "v1234567890/profiles/user_avatar.jpg"
    )

    assert requested["url"] == "https://api.cloudinary.com/v1_1/demo-cloud/image/destroy"
    assert requested["data"]["public_id"] == "profiles/user_avatar"
    assert requested["data"]["timestamp"] == 123456


def test_account_deletion_does_not_delete_unmanaged_external_photo():
    users._delete_profile_photo_asset("https://images.example.test/avatar.jpg")


def test_delete_account_removes_reports_and_related_data(
    deletion_session,
    monkeypatch,
):
    uid = "firebase-delete-user"
    monkeypatch.setenv("SUPABASE_URL", "https://supabase.example.test")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "test-service-key")
    deletion_session.add_all(
        [
            User(firebase_uid=uid, email="person@example.com"),
            UsernameReservation(username="person_123", firebase_uid=uid),
            DeviceToken(firebase_uid=uid, token="device-token"),
            UserNotification(
                firebase_uid=uid,
                found_item_id="found-1",
                title="Match",
                body="Potential match",
                score=0.9,
            ),
            AdminMatchAlert(
                found_item_id="found-1",
                lost_item_id="lost-1",
                found_title="Found bag",
                lost_title="Lost bag",
                confidence=0.9,
                reason="Potential match",
            ),
            EmailOTPChallenge(
                firebase_uid=uid,
                email="person@example.com",
                code_digest="0" * 64,
                sent_at=100,
                expires_at=200,
                attempts=0,
                sent=True,
            ),
            LostItem(
                id="lost-1",
                created_by=uid,
                title="Lost bag",
                description="Black bag",
                lat=1.0,
                lng=2.0,
                image_url="https://api.example.test/static/uploads/report.jpg",
            ),
            FoundItem(
                id="found-1",
                created_by=uid,
                title="Found bag",
                description="Black bag",
                lat=3.0,
                lng=4.0,
                image_url="https://api.example.test/static/uploads/report.jpg",
            ),
            SocialPublication(
                id="social-lost-1",
                report_id="lost-1",
                report_type="lost",
                provider="facebook",
                status="published",
                external_post_id="public-post-1",
            ),
            SocialPublication(
                id="social-found-1",
                report_id="found-1",
                report_type="found",
                provider="instagram",
                status="pending",
            ),
        ]
    )
    deletion_session.commit()

    monkeypatch.setattr(users, "_firebase_app", lambda: object())
    monkeypatch.setattr(users, "_delete_firestore_account_copies", lambda *args: None)
    monkeypatch.setattr(users, "_delete_profile_photo_asset", lambda _: None)
    monkeypatch.setattr(users, "_delete_supabase_account_matching_data", lambda _: None)
    auth_deleted_after_database_commit = []

    def delete_firebase_user(target_uid, app):
        auth_deleted_after_database_commit.append(
            deletion_session.query(User).filter_by(firebase_uid=target_uid).count() == 0
        )

    monkeypatch.setattr(users.firebase_auth, "delete_user", delete_firebase_user)
    response = users.delete_account(
        response=users.Response(),
        session=deletion_session,
        uid=uid,
    )

    assert response.status_code == 204
    assert auth_deleted_after_database_commit == [True]
    assert deletion_session.query(User).filter_by(firebase_uid=uid).count() == 0
    assert deletion_session.get(EmailOTPChallenge, uid) is None
    assert deletion_session.get(LostItem, "lost-1") is None
    assert deletion_session.get(FoundItem, "found-1") is None
    assert deletion_session.query(SocialPublication).count() == 0
    assert deletion_session.query(AdminMatchAlert).count() == 0
    assert deletion_session.query(DeviceToken).filter_by(firebase_uid=uid).count() == 0
    assert deletion_session.query(UserNotification).filter_by(firebase_uid=uid).count() == 0
    assert deletion_session.query(UsernameReservation).filter_by(firebase_uid=uid).count() == 0
