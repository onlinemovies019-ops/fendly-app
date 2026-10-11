import io

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

import database
import main
from models import AdoptionApplication, AdoptionListing, Base, User, UserNotification
from routers import admin as admin_module
from routers import adoptions as adoptions_module


@pytest.fixture
def adoption_client(monkeypatch):
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    session_factory = sessionmaker(bind=engine, autoflush=False, autocommit=False)

    def override_get_db():
        with session_factory() as session:
            yield session

    async def allow_content(*args, **kwargs):
        return None

    async def store_image(data, key, content_type):
        return f"https://storage.example/{key}"

    current_uid = ["lister"]
    main.app.dependency_overrides[database.get_db] = override_get_db
    main.app.dependency_overrides[adoptions_module.get_current_user] = lambda: current_uid[0]
    main.app.dependency_overrides[admin_module.get_current_user] = lambda: current_uid[0]
    monkeypatch.setattr(adoptions_module, "moderate_content", allow_content)
    monkeypatch.setattr(adoptions_module, "_check_image", lambda data, mime_type: "jpg")
    monkeypatch.setattr(adoptions_module, "store_adoption_image", store_image)
    monkeypatch.setattr(
        adoptions_module,
        "render_adoption_poster",
        lambda *_args, **_kwargs: b"framed adoption poster",
    )
    for setting in (
        "META_FACEBOOK_PAGE_ID",
        "META_INSTAGRAM_ACCOUNT_ID",
        "META_PAGE_ACCESS_TOKEN",
        "APP_DOWNLOAD_URL",
    ):
        monkeypatch.delenv(setting, raising=False)
    client = TestClient(main.app)
    yield client, session_factory, current_uid
    client.close()
    main.app.dependency_overrides.pop(database.get_db, None)
    main.app.dependency_overrides.pop(adoptions_module.get_current_user, None)
    main.app.dependency_overrides.pop(admin_module.get_current_user, None)
    engine.dispose()


def _create_listing(
    client: TestClient,
    *,
    auto_post_social: str = "false",
    image_count: int = 1,
):
    response = client.post(
        "/api/adoptions",
        data={
            "pet_name": "Milo",
            "species": "dog",
            "breed": "Indie",
            "age_months": "18",
            "gender": "male",
            "health_status": "Vaccinated",
            "description": "Friendly and active.",
            "location_city": "Pune",
            "auto_post_social": auto_post_social,
        },
        files=[
            ("images", (f"milo-{index}.jpg", io.BytesIO(b"valid image"), "image/jpeg"))
            for index in range(image_count)
        ],
    )
    return response


def test_adoption_listing_rejects_more_than_three_photos(adoption_client):
    client, _, _ = adoption_client

    response = _create_listing(client, image_count=4)

    assert response.status_code == 422


def test_lister_can_edit_and_delete_listing_but_other_users_cannot(adoption_client, monkeypatch):
    client, session_factory, current_uid = adoption_client
    deleted_images = []
    monkeypatch.setattr(adoptions_module, "delete_adoption_image", deleted_images.append)

    created = _create_listing(client)
    assert created.status_code == 201, created.text
    listing_id = created.json()["id"]
    update = {
        "pet_name": "Milo Updated",
        "species": "dog",
        "breed": "Indie",
        "age_months": 20,
        "gender": "male",
        "health_status": "Vaccinated",
        "description": "Friendly and active.",
        "location_city": "Pune",
    }

    current_uid[0] = "another-user"
    assert client.put(f"/api/adoptions/{listing_id}", json=update).status_code == 403
    assert client.delete(f"/api/adoptions/{listing_id}").status_code == 403

    current_uid[0] = "lister"
    edited = client.put(f"/api/adoptions/{listing_id}", json=update)
    assert edited.status_code == 200, edited.text
    assert edited.json()["pet_name"] == "Milo Updated"

    with Session(session_factory.kw["bind"]) as session:
        session.add(
            AdoptionApplication(
                id="application-to-delete",
                listing_id=listing_id,
                applicant_user_id="applicant",
                home_type="House",
                pet_experience="Experienced",
                contact_phone="+911234567890",
                status="submitted",
            )
        )
        session.add(
            UserNotification(
                firebase_uid="applicant",
                found_item_id=None,
                adoption_listing_id=listing_id,
                notification_type="adoption_application_update",
                title="Application update",
                body="Test notification",
                score=0.0,
            )
        )
        session.commit()
        expected_image_urls = list(session.get(AdoptionListing, listing_id).images)

    deleted = client.delete(f"/api/adoptions/{listing_id}")
    assert deleted.status_code == 200, deleted.text
    assert deleted.json() == {"status": "deleted"}
    assert deleted_images == expected_image_urls
    with Session(session_factory.kw["bind"]) as session:
        assert session.get(AdoptionListing, listing_id) is None
        assert session.get(AdoptionApplication, "application-to-delete") is None
        assert session.scalar(
            select(UserNotification).where(
                UserNotification.adoption_listing_id == listing_id
            )
        ) is None


def test_admin_can_list_and_delete_adoption_with_meta_posts(
    adoption_client,
    monkeypatch,
):
    client, session_factory, current_uid = adoption_client
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "lister")
    deleted_images = []
    monkeypatch.setattr(admin_module, "delete_adoption_image", deleted_images.append)

    created = _create_listing(client, auto_post_social="true")
    assert created.status_code == 201, created.text
    listing_id = created.json()["id"]
    with Session(session_factory.kw["bind"]) as session:
        listing = session.get(AdoptionListing, listing_id)
        listing.facebook_post_id = "facebook-post-id"
        listing.instagram_post_id = "instagram-post-id"
        session.add(
            AdoptionApplication(
                id="admin-delete-application",
                listing_id=listing_id,
                applicant_user_id="applicant",
                home_type="House",
                pet_experience="Experienced",
                contact_phone="+911234567890",
                status="submitted",
            )
        )
        session.add(
            UserNotification(
                firebase_uid="applicant",
                found_item_id=None,
                adoption_listing_id=listing_id,
                notification_type="adoption_application_update",
                title="Application update",
                body="Test notification",
                score=0.0,
            )
        )
        session.commit()
        expected_image_urls = list(listing.images)
        expected_image_urls.append(listing.social_post_image_url)

    current_uid[0] = "not-admin"
    assert client.get("/api/admin/adoptions").status_code == 403
    assert client.delete(f"/api/admin/adoptions/{listing_id}").status_code == 403
    current_uid[0] = "lister"

    listed = client.get("/api/admin/adoptions")
    assert listed.status_code == 200, listed.text
    assert listed.json()[0]["facebook_post_id"] == "facebook-post-id"
    assert listed.json()[0]["instagram_post_id"] == "instagram-post-id"

    calls = []

    def remove_meta_posts(listing, on_deletion, **accounts):
        calls.append((listing.id, listing.facebook_post_id, listing.instagram_post_id))
        on_deletion("facebook")
        on_deletion("instagram")
        return []

    monkeypatch.setattr(admin_module, "delete_adoption_publications", remove_meta_posts)

    deleted = client.delete(f"/api/admin/adoptions/{listing_id}")

    assert deleted.status_code == 200, deleted.text
    assert deleted.json() == {"status": "deleted", "platform_failures": []}
    assert calls == [(listing_id, "facebook-post-id", "instagram-post-id")]
    assert deleted_images == expected_image_urls
    with Session(session_factory.kw["bind"]) as session:
        assert session.get(AdoptionListing, listing_id) is None
        assert session.get(AdoptionApplication, "admin-delete-application") is None
        assert session.scalar(
            select(UserNotification).where(
                UserNotification.adoption_listing_id == listing_id
            )
        ) is None


def test_admin_keeps_listing_when_meta_post_removal_fails(adoption_client, monkeypatch):
    client, session_factory, _ = adoption_client
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "lister")

    created = _create_listing(client)
    listing_id = created.json()["id"]
    with Session(session_factory.kw["bind"]) as session:
        listing = session.get(AdoptionListing, listing_id)
        listing.facebook_post_id = "facebook-post-id"
        session.commit()

    monkeypatch.setattr(
        admin_module,
        "delete_adoption_publications",
        lambda *_args, **_kwargs: ["Facebook: permission denied"],
    )
    deleted_images = []
    monkeypatch.setattr(admin_module, "delete_adoption_image", deleted_images.append)

    response = client.delete(f"/api/admin/adoptions/{listing_id}")

    assert response.status_code == 503
    assert response.json()["detail"]["platform_failures"] == [
        "Facebook: permission denied"
    ]
    assert deleted_images == []
    with Session(session_factory.kw["bind"]) as session:
        assert session.get(AdoptionListing, listing_id) is not None


def test_public_feed_filters_and_hides_owner_and_social_fields(adoption_client):
    client, session_factory, _ = adoption_client
    with Session(session_factory.kw["bind"]) as session:
        session.add_all(
            [
                AdoptionListing(
                    id="pune-dog",
                    lister_user_id="owner",
                    pet_name="Milo",
                    species="dog",
                    age_months=18,
                    gender="male",
                    health_status="Vaccinated",
                    description="Friendly",
                    location_city="Pune",
                    images=["https://images.example/milo.jpg"],
                    auto_post_social=True,
                    social_post_status="published",
                    facebook_post_id="fb-123",
                ),
                AdoptionListing(
                    id="mumbai-cat",
                    lister_user_id="other",
                    pet_name="Luna",
                    species="cat",
                    age_months=48,
                    gender="female",
                    health_status="Healthy",
                    description="Calm",
                    location_city="Mumbai",
                    images=[],
                ),
            ]
        )
        session.commit()

    response = client.get(
        "/api/adoptions?species=dog&city=Pune&min_age_months=12&max_age_months=24"
    )

    assert response.status_code == 200
    assert [listing["id"] for listing in response.json()] == ["pune-dog"]
    assert "lister_user_id" not in response.json()[0]
    assert "facebook_post_id" not in response.json()[0]
    assert "social_post_status" not in response.json()[0]


def test_applications_may_be_multiple_pending_and_only_lister_can_approve(adoption_client):
    client, session_factory, current_uid = adoption_client
    try:
        listing_response = _create_listing(client)
        assert listing_response.status_code == 201, listing_response.text
        listing_id = listing_response.json()["id"]
        with Session(session_factory.kw["bind"]) as session:
            session.add(
                User(
                    firebase_uid="lister",
                    mobile="+911234560000",
                    mobile_verified=True,
                )
            )
            session.commit()

        current_uid[0] = "applicant-one"
        first = client.post(
            f"/api/adoptions/{listing_id}/apply",
            data={
                "home_type": "House",
                "pet_experience": "Have cared for dogs.",
                "contact_phone": "+911234567890",
            },
        )
        assert first.status_code == 201, first.text

        current_uid[0] = "applicant-two"
        second = client.post(
            f"/api/adoptions/{listing_id}/apply",
            data={
                "home_type": "Apartment",
                "pet_experience": "First-time pet owner.",
                "contact_phone": "+911234567891",
            },
        )
        assert second.status_code == 201, second.text

        denied = client.get(f"/api/adoptions/{listing_id}/applications")
        assert denied.status_code == 403

        current_uid[0] = "lister"
        applications = client.get(f"/api/adoptions/{listing_id}/applications")
        assert applications.status_code == 200
        assert len(applications.json()) == 2
        applicant_id = first.json()["id"]
        approval = client.post(
            f"/api/adoptions/applications/{applicant_id}/decision",
            data={"decision": "approved"},
        )
        assert approval.status_code == 200

        current_uid[0] = "applicant-one"
        my_applications = client.get("/api/adoptions/applications/mine")
        assert my_applications.status_code == 200
        assert my_applications.json()[0]["lister_contact_phone"] == "+911234560000"
        notification_inbox = client.get("/api/notifications")
        assert notification_inbox.status_code == 200
        assert notification_inbox.json()["notifications"][0]["adoption_listing_id"] == listing_id

        with Session(session_factory.kw["bind"]) as session:
            listing = session.get(AdoptionListing, listing_id)
            app_statuses = session.scalars(
                select(AdoptionApplication.status).where(
                    AdoptionApplication.listing_id == listing_id
                )
            ).all()
            notifications = session.scalars(
                select(UserNotification).where(
                    UserNotification.adoption_listing_id == listing_id
                )
            ).all()
        assert listing.status == "adopted"
        assert sorted(app_statuses) == ["approved", "rejected"]
        assert len(notifications) == 2
        assert all(notification.score == 0 for notification in notifications)
    finally:
        current_uid[0] = "lister"


def test_social_opt_in_failure_does_not_remove_public_listing(adoption_client):
    client, session_factory, _ = adoption_client
    response = _create_listing(client, auto_post_social="true")

    assert response.status_code == 201, response.text
    assert response.json()["status"] == "available"
    assert response.json()["social_post_status"] == "failed"
    with Session(session_factory.kw["bind"]) as session:
        listing = session.get(AdoptionListing, response.json()["id"])
    assert listing is not None
    assert listing.auto_post_social is True
    assert "no connected facebook page" in listing.social_post_error.lower()
    assert listing.social_post_image_url is not None


def test_social_poster_url_is_returned_only_to_listing_owner(adoption_client):
    client, _, _ = adoption_client
    created = _create_listing(client, auto_post_social="true")
    assert created.status_code == 201, created.text
    poster_url = created.json()["social_post_image_url"]

    mine = client.get("/api/adoptions/mine")
    public_feed = client.get("/api/adoptions")

    assert mine.status_code == 200
    assert mine.json()[0]["social_post_image_url"] == poster_url
    assert public_feed.status_code == 200
    assert "social_post_image_url" not in public_feed.json()[0]
