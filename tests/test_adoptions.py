import io

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

import database
import main
from models import AdoptionApplication, AdoptionListing, Base, User, UserNotification
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
    monkeypatch.setattr(adoptions_module, "moderate_content", allow_content)
    monkeypatch.setattr(adoptions_module, "_check_image", lambda data, mime_type: "jpg")
    monkeypatch.setattr(adoptions_module, "store_adoption_image", store_image)
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
    engine.dispose()


def _create_listing(client: TestClient, *, auto_post_social: str = "false"):
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
        files={"images": ("milo.jpg", io.BytesIO(b"valid image"), "image/jpeg")},
    )
    return response


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
    assert "not configured" in listing.social_post_error.lower()
