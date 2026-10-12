import asyncio
from datetime import datetime, timezone

import pytest
from fastapi import HTTPException
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

import database
import main
from routers import admin as admin_module
from routers import items as items_module
from routers import users as users_module
from models import (
    AdminMatchAlert,
    Base,
    ContentReport,
    FoundItem,
    LostItem,
    User,
    UserNotification,
)
from routers.items import list_my_items


@pytest.fixture
def reports_client(monkeypatch):
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

    monkeypatch.setenv("APP_SECRET_KEY", "test-public-reports-secret-012345")
    main.app.dependency_overrides[database.get_db] = override_get_db
    client = TestClient(main.app)
    yield client, session_factory
    client.close()
    main.app.dependency_overrides.pop(database.get_db, None)
    engine.dispose()


def test_user_report_listing_is_not_exposed(reports_client):
    client, _ = reports_client
    assert client.get("/api/reports").status_code == 404


def test_static_uploads_are_not_publicly_served(reports_client):
    client, _ = reports_client

    response = client.get("/static/uploads/private-report.jpg")

    assert response.status_code == 404


def test_owned_report_community_poster_uses_the_shared_poster_renderer(
    reports_client,
    monkeypatch,
):
    client, session_factory = reports_client

    generated = []

    async def render_poster(report, report_type, image_urls=None):
        generated.append((report.id, report_type, image_urls))
        return b"canonical-poster"

    monkeypatch.setattr("social_poster.render_report_poster", render_poster)
    monkeypatch.setattr(
        items_module,
        "refresh_private_image_url",
        lambda image_url: f"signed:{image_url}" if image_url else None,
    )
    main.app.dependency_overrides[items_module.get_current_user] = lambda: "report-owner"
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(
                LostItem(
                    id="poster-report",
                    created_by="report-owner",
                    title="Lost squirrel",
                    description="Brown squirrel",
                    report_date="10/10/2026",
                    image_url="photo-1",
                    image_urls=["photo-1", "photo-2", "photo-3"],
                    lat=0,
                    lng=0,
                )
            )
            session.commit()

        response = client.get("/api/items/lost/poster-report/community-poster")
        assert response.status_code == 200
        assert response.content == b"canonical-poster"
        assert response.headers["content-type"] == "image/jpeg"
        assert response.headers["cache-control"] == "no-store"
        assert generated == [
            ("poster-report", "lost", ["signed:photo-1", "signed:photo-2", "signed:photo-3"])
        ]
    finally:
        main.app.dependency_overrides.pop(items_module.get_current_user, None)


def test_community_poster_endpoint_does_not_expose_another_users_report(reports_client):
    client, session_factory = reports_client
    main.app.dependency_overrides[items_module.get_current_user] = lambda: "different-user"
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(
                LostItem(
                    id="private-poster-report",
                    created_by="report-owner",
                    title="Lost bag",
                    description="Blue bag",
                    lat=0,
                    lng=0,
                )
            )
            session.commit()
        response = client.get("/api/items/lost/private-poster-report/community-poster")
        assert response.status_code == 404
    finally:
        main.app.dependency_overrides.pop(items_module.get_current_user, None)


def test_user_content_flagging_is_not_exposed(reports_client):
    client, _ = reports_client
    assert client.get("/api/admin/content-reports").status_code == 404
    assert client.post("/api/admin/private-images/migrate").status_code == 404
    assert client.post(
        "/api/items/found/report-id/reports",
        json={"reason": "spam"},
    ).status_code == 404


def test_admin_removal_deletes_report_and_linked_app_records(reports_client, monkeypatch):
    client, session_factory = reports_client
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "admin-uid")
    monkeypatch.delenv("SUPABASE_URL", raising=False)
    monkeypatch.delenv("SUPABASE_SERVICE_ROLE_KEY", raising=False)
    main.app.dependency_overrides[admin_module.get_current_user] = lambda: "admin-uid"
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add_all([
                LostItem(
                    id="fake-report",
                    created_by="report-owner",
                    title="Fake lost cat",
                    description="Fake",
                    lat=0,
                    lng=0,
                ),
                FoundItem(
                    id="other-report",
                    created_by="report-owner",
                    title="Found dog",
                    description="Real",
                    lat=0,
                    lng=0,
                ),
                AdminMatchAlert(
                    found_item_id="other-report",
                    lost_item_id="fake-report",
                    found_title="Found dog",
                    lost_title="Fake lost cat",
                    confidence=0.9,
                    reason="Potential match",
                ),
                ContentReport(
                    reporter_uid="admin-uid",
                    report_type="lost",
                    report_id="fake-report",
                    reason="fraud",
                ),
                UserNotification(
                    firebase_uid="report-owner",
                    found_item_id="other-report",
                    title="Possible match",
                    body="A possible match was found.",
                    score=0.9,
                ),
            ])
            session.commit()

        response = client.delete("/api/admin/items/lost/fake-report")

        assert response.status_code == 200
        assert response.json() == {
            "status": "removed",
            "match_alert_failures": [],
        }
        with Session(session_factory.kw["bind"]) as session:
            assert session.get(LostItem, "fake-report") is None
            assert session.get(FoundItem, "other-report") is not None
            assert session.query(AdminMatchAlert).count() == 0
            assert session.query(ContentReport).count() == 0
            assert session.query(UserNotification).count() == 1
    finally:
        main.app.dependency_overrides.pop(admin_module.get_current_user, None)


def test_admin_report_deletion_does_not_call_social_apis(reports_client, monkeypatch):
    client, session_factory = reports_client
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "admin-uid")
    main.app.dependency_overrides[admin_module.get_current_user] = lambda: "admin-uid"
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(LostItem(
                id="no-social-report",
                created_by="report-owner",
                title="Lost cat",
                description="Details",
                lat=0,
                lng=0,
            ))
            session.commit()

        response = client.delete("/api/admin/items/lost/no-social-report")

        assert response.status_code == 200
        assert response.json()["status"] == "removed"
        with Session(session_factory.kw["bind"]) as session:
            assert session.get(LostItem, "no-social-report") is None
    finally:
        main.app.dependency_overrides.pop(admin_module.get_current_user, None)


def test_owner_report_deletion_does_not_call_social_apis(reports_client):
    client, session_factory = reports_client
    main.app.dependency_overrides[items_module.get_current_user] = lambda: "report-owner"
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(LostItem(
                id="owner-report",
                created_by="report-owner",
                title="Lost cat",
                description="Details",
                lat=0,
                lng=0,
                created_at=datetime.now(timezone.utc),
            ))
            session.commit()

        listed_reports = client.get("/api/items/mine")
        assert listed_reports.status_code == 200
        assert listed_reports.json()[0]["can_delete"] is True

        response = client.delete("/api/items/lost/owner-report")

        assert response.status_code == 204
        with Session(session_factory.kw["bind"]) as session:
            assert session.get(LostItem, "owner-report") is None
    finally:
        main.app.dependency_overrides.pop(items_module.get_current_user, None)


def test_owner_resolution_stores_optional_consent_and_reopening_revokes_it(
    reports_client,
):
    client, session_factory = reports_client
    main.app.dependency_overrides[items_module.get_current_user] = lambda: "report-owner"
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(
                FoundItem(
                    id="reunited-found-report",
                    created_by="report-owner",
                    title="Found dog",
                    description="Brown dog",
                    lat=0,
                    lng=0,
                )
            )
            session.commit()

        resolved = client.patch(
            "/api/items/found/reunited-found-report/resolution",
            json={"resolved": True, "allow_success_story_sharing": True},
        )
        assert resolved.status_code == 200
        assert resolved.json()["status"] == "REUNITED"
        assert resolved.json()["allow_success_story_sharing"] is True

        listed = client.get("/api/items/mine")
        assert listed.status_code == 200
        assert listed.json()[0]["status"] == "REUNITED"
        assert listed.json()[0]["allow_success_story_sharing"] is True
        assert listed.json()[0]["workflow_stage"] == 4

        reopened = client.patch(
            "/api/items/found/reunited-found-report/resolution",
            json={"resolved": False, "allow_success_story_sharing": True},
        )
        assert reopened.status_code == 200
        assert reopened.json()["status"] == "FOUND"
        assert reopened.json()["allow_success_story_sharing"] is False
    finally:
        main.app.dependency_overrides.pop(items_module.get_current_user, None)


def test_only_report_owner_can_update_resolution(reports_client):
    client, session_factory = reports_client
    main.app.dependency_overrides[items_module.get_current_user] = lambda: "different-user"
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(
                LostItem(
                    id="private-resolution-report",
                    created_by="report-owner",
                    title="Lost watch",
                    description="Silver",
                    lat=0,
                    lng=0,
                )
            )
            session.commit()

        response = client.patch(
            "/api/items/lost/private-resolution-report/resolution",
            json={"resolved": True, "allow_success_story_sharing": True},
        )

        assert response.status_code == 404
        with Session(session_factory.kw["bind"]) as session:
            report = session.get(LostItem, "private-resolution-report")
            assert report.status == "LOST"
            assert report.allow_success_story_sharing is False
    finally:
        main.app.dependency_overrides.pop(items_module.get_current_user, None)


def test_admin_success_stories_returns_only_resolved_opted_in_minimal_fields(
    reports_client,
    monkeypatch,
):
    client, session_factory = reports_client
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "admin-uid")
    main.app.dependency_overrides[admin_module.get_current_user] = lambda: "admin-uid"
    monkeypatch.setattr(
        admin_module,
        "refresh_private_image_urls",
        lambda image_urls: [
            "https://signed.example/photo" if image_url else None
            for image_url in image_urls
        ],
    )
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(
                User(firebase_uid="story-owner", city="Pune")
            )
            session.add_all(
                [
                    LostItem(
                        id="opted-story",
                        created_by="story-owner",
                        title="Lost bag",
                        description="Private report text",
                        category="bag",
                        lat=0,
                        lng=0,
                        status="REUNITED",
                        allow_success_story_sharing=True,
                        image_url="private://photo",
                    ),
                    FoundItem(
                        id="not-opted-story",
                        created_by="story-owner",
                        title="Found phone",
                        description="Private found report",
                        category="phone",
                        lat=0,
                        lng=0,
                        status="REUNITED",
                        allow_success_story_sharing=False,
                    ),
                    LostItem(
                        id="active-opted-story",
                        created_by="story-owner",
                        title="Lost keys",
                        description="Still missing",
                        category="keys",
                        lat=0,
                        lng=0,
                        allow_success_story_sharing=True,
                    ),
                ]
            )
            session.commit()

        response = client.get("/api/admin/success-stories")

        assert response.status_code == 200
        assert response.json() == [
            {
                "type": "lost",
                "category": "bag",
                "city": "Pune",
                "photo_url": "https://signed.example/photo",
            }
        ]
    finally:
        main.app.dependency_overrides.pop(admin_module.get_current_user, None)


def test_my_reports_only_marks_recent_reports_as_deletable(reports_client, monkeypatch):
    client, session_factory = reports_client
    main.app.dependency_overrides[items_module.get_current_user] = lambda: "report-owner"
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add_all([
                LostItem(
                    id="recent-report",
                    created_by="report-owner",
                    title="Lost cat",
                    description="Recent",
                    lat=0,
                    lng=0,
                    created_at=datetime.now(timezone.utc),
                ),
                LostItem(
                    id="old-report",
                    created_by="report-owner",
                    title="Lost dog",
                    description="Old",
                    lat=0,
                    lng=0,
                    created_at=datetime(2025, 1, 1, tzinfo=timezone.utc),
                ),
            ])
            session.commit()

        response = client.get("/api/items/mine")

        assert response.status_code == 200
        reports = {report["id"]: report for report in response.json()}
        assert reports["recent-report"]["can_delete"] is True
        assert reports["old-report"]["can_delete"] is False
    finally:
        main.app.dependency_overrides.pop(items_module.get_current_user, None)


def test_my_reports_batches_private_photo_signing(reports_client, monkeypatch):
    client, session_factory = reports_client
    main.app.dependency_overrides[items_module.get_current_user] = lambda: "report-owner"
    signed_batches = []

    def sign_urls(image_urls):
        signed_batches.append(list(image_urls))
        return [
            f"https://signed.example/{index}" if image_url else None
            for index, image_url in enumerate(image_urls)
        ]

    monkeypatch.setattr(items_module, "refresh_private_image_urls", sign_urls)
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add_all([
                LostItem(
                    id="photo-report-one",
                    created_by="report-owner",
                    title="Lost cat",
                    description="First report",
                    image_url="storage://report-images/one/primary.jpg",
                    image_urls=[
                        "storage://report-images/one/primary.jpg",
                        "storage://report-images/one/extra.jpg",
                    ],
                    lat=0,
                    lng=0,
                ),
                FoundItem(
                    id="photo-report-two",
                    created_by="report-owner",
                    title="Found dog",
                    description="Second report",
                    image_url="storage://report-images/two/photo.jpg",
                    lat=0,
                    lng=0,
                ),
            ])
            session.commit()

        response = client.get("/api/items/mine")

        assert response.status_code == 200, response.text
        reports = {report["id"]: report for report in response.json()}
        assert signed_batches == [[
            "storage://report-images/one/primary.jpg",
            "storage://report-images/one/extra.jpg",
            "storage://report-images/one/primary.jpg",
            "storage://report-images/two/photo.jpg",
            "storage://report-images/two/photo.jpg",
        ]]
        assert reports["photo-report-one"]["image_url"] == "https://signed.example/2"
        assert reports["photo-report-one"]["image_urls"] == [
            "https://signed.example/0",
            "https://signed.example/1",
        ]
        assert reports["photo-report-two"]["image_url"] == "https://signed.example/4"
        assert reports["photo-report-two"]["image_urls"] == ["https://signed.example/3"]
    finally:
        main.app.dependency_overrides.pop(items_module.get_current_user, None)


def test_non_admin_cannot_remove_report(reports_client, monkeypatch):
    client, session_factory = reports_client
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "admin-uid")
    main.app.dependency_overrides[admin_module.get_current_user] = lambda: "user-uid"
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(LostItem(
                id="protected-report",
                created_by="report-owner",
                title="Lost item",
                description="Details",
                lat=0,
                lng=0,
            ))
            session.commit()

        response = client.delete("/api/admin/items/lost/protected-report")

        assert response.status_code == 403
        with Session(session_factory.kw["bind"]) as session:
            assert session.get(LostItem, "protected-report") is not None
    finally:
        main.app.dependency_overrides.pop(admin_module.get_current_user, None)


def test_report_image_urls_must_be_private_supabase_signed_urls(monkeypatch):
    monkeypatch.setenv("ENVIRONMENT", "development")
    monkeypatch.setenv("SUPABASE_URL", "https://project.supabase.co")

    items_module._validate_report_image_urls([
        "https://project.supabase.co/storage/v1/object/sign/report-images/"
        "image.jpg?token=short-lived"
    ])

    with pytest.raises(HTTPException) as error:
        items_module._validate_report_image_urls([
            "https://project.supabase.co/storage/v1/object/public/report-images/image.jpg"
        ])
    assert error.value.status_code == 400

    with pytest.raises(HTTPException) as error:
        items_module._validate_report_image_urls([
            "http://127.0.0.1:8000/static/uploads/image.jpg"
        ])
    assert error.value.status_code == 400


def test_block_account_endpoints_are_not_exposed(reports_client):
    client, _ = reports_client

    assert client.get("/api/users/blocked-users").status_code == 404
    assert client.post("/api/users/blocked-users/found/report-id").status_code == 404


def test_my_reports_exposes_match_and_reunited_workflow_stages(reports_client):
    _, session_factory = reports_client
    with Session(session_factory.kw["bind"]) as session:
        reports = [
            LostItem(
                id="lost-pending",
                created_by="owner",
                title="Lost pending report",
                description="Pending match",
                lat=21.1,
                lng=79.0,
                status="LOST",
            ),
            FoundItem(
                id="found-pending",
                created_by="owner",
                title="Found pending report",
                description="Potential match",
                lat=21.1,
                lng=79.0,
            ),
            LostItem(
                id="lost-recovered",
                created_by="owner",
                title="Recovered report",
                description="Successfully reunited",
                lat=21.1,
                lng=79.0,
                status="RECOVERED",
            ),
            FoundItem(
                id="found-recovered",
                created_by="owner",
                title="Recovered found report",
                description="Successfully reunited",
                lat=21.1,
                lng=79.0,
            ),
            LostItem(
                id="other-owner-recovered",
                created_by="another-owner",
                title="Another owner's recovered report",
                description="Successfully reunited",
                lat=21.1,
                lng=79.0,
                status="RECOVERED",
            ),
            FoundItem(
                id="found-for-other-owner",
                created_by="owner",
                title="Found report for another owner",
                description="Successfully reunited",
                lat=21.1,
                lng=79.0,
            ),
            LostItem(
                id="lost-rejected",
                created_by="owner",
                title="Rejected match report",
                description="No confirmed match",
                lat=21.1,
                lng=79.0,
                status="LOST",
            ),
        ]
        session.add_all(reports)
        session.add_all([
            AdminMatchAlert(
                found_item_id="found-pending",
                lost_item_id="lost-pending",
                found_title="Found pending report",
                lost_title="Lost pending report",
                confidence=0.9,
                reason="Potential match",
                review_status="pending",
            ),
            AdminMatchAlert(
                found_item_id="found-recovered",
                lost_item_id="lost-recovered",
                found_title="Found recovered report",
                lost_title="Lost recovered report",
                confidence=0.95,
                reason="Confirmed and reunited",
                review_status="confirmed",
            ),
            AdminMatchAlert(
                found_item_id="found-for-other-owner",
                lost_item_id="other-owner-recovered",
                found_title="Found report for another owner",
                lost_title="Another owner's recovered report",
                confidence=0.95,
                reason="Confirmed and reunited",
                review_status="confirmed",
            ),
            AdminMatchAlert(
                found_item_id="other-found",
                lost_item_id="lost-rejected",
                found_title="Unrelated report",
                lost_title="Rejected match report",
                confidence=0.3,
                reason="Rejected potential match",
                review_status="rejected",
            ),
        ])
        session.commit()

        reports_by_id = {
            report["id"]: report
            for report in asyncio.run(list_my_items(session=session, uid="owner"))
        }

    assert reports_by_id["lost-pending"]["workflow_stage"] == 2
    assert reports_by_id["found-pending"]["workflow_stage"] == 2
    assert reports_by_id["lost-recovered"]["workflow_stage"] == 4
    assert reports_by_id["found-recovered"]["workflow_stage"] == 4
    assert reports_by_id["found-for-other-owner"]["workflow_stage"] == 4
    assert reports_by_id["lost-rejected"]["workflow_stage"] == 2
