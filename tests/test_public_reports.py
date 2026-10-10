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
    SocialPublication,
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


def test_owned_report_community_poster_uses_the_shared_poster_renderer(
    reports_client,
    monkeypatch,
):
    client, session_factory = reports_client

    generated = []

    async def render_poster(report, report_type):
        generated.append((report.id, report_type))
        return b"canonical-poster"

    monkeypatch.setattr("social_poster.render_report_poster", render_poster)
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
        assert generated == [("poster-report", "lost")]
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
    monkeypatch.setattr(
        "social_publishing.remove_report_publications",
        lambda session, report_id, report_type: [],
    )
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
            "platform_failures": [],
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


def test_admin_report_remains_visible_until_all_social_posts_are_removed(
    reports_client, monkeypatch
):
    client, session_factory = reports_client
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "admin-uid")
    monkeypatch.delenv("SUPABASE_URL", raising=False)
    monkeypatch.delenv("SUPABASE_SERVICE_ROLE_KEY", raising=False)
    main.app.dependency_overrides[admin_module.get_current_user] = lambda: "admin-uid"
    monkeypatch.setattr(
        "social_publishing.remove_report_publications",
        lambda session, report_id, report_type: [
            "instagram post could not be removed: permission denied"
        ],
    )
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(LostItem(
                id="partially-published-report",
                created_by="report-owner",
                title="Lost cat",
                description="Details",
                lat=0,
                lng=0,
                social_share_consent=True,
            ))
            session.commit()

        response = client.delete(
            "/api/admin/items/lost/partially-published-report"
        )

        assert response.status_code == 502
        assert "NOT removed from Fendly" in response.json()["detail"]
        assert "instagram post" in response.json()["detail"]
        with Session(session_factory.kw["bind"]) as session:
            report = session.get(LostItem, "partially-published-report")
            assert report is not None
            assert report.social_share_consent is False
    finally:
        main.app.dependency_overrides.pop(admin_module.get_current_user, None)


def test_owner_report_remains_visible_when_social_post_deletion_fails(
    reports_client, monkeypatch
):
    client, session_factory = reports_client
    main.app.dependency_overrides[items_module.get_current_user] = lambda: "report-owner"
    monkeypatch.setattr(
        "social_publishing.remove_report_publications",
        lambda session, report_id, report_type: [
            "facebook post could not be removed: permission denied"
        ],
    )
    try:
        with Session(session_factory.kw["bind"]) as session:
            session.add(LostItem(
                id="owner-social-report",
                created_by="report-owner",
                title="Lost cat",
                description="Details",
                lat=0,
                lng=0,
                social_share_consent=True,
                created_at=datetime.now(timezone.utc),
            ))
            session.commit()

        listed_reports = client.get("/api/items/mine")
        assert listed_reports.status_code == 200
        assert listed_reports.json()[0]["can_delete"] is True

        response = client.delete("/api/items/lost/owner-social-report")

        assert response.status_code == 502
        assert "facebook post" in response.json()["detail"]
        with Session(session_factory.kw["bind"]) as session:
            report = session.get(LostItem, "owner-social-report")
            assert report is not None
            assert report.social_share_consent is False
    finally:
        main.app.dependency_overrides.pop(items_module.get_current_user, None)


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


def test_local_report_image_urls_are_only_allowed_for_loopback_in_development(monkeypatch):
    monkeypatch.setenv("ENVIRONMENT", "development")
    monkeypatch.setenv("PUBLIC_BASE_URL", "http://127.0.0.1:8000")
    monkeypatch.delenv("SUPABASE_URL", raising=False)
    monkeypatch.delenv("CLOUDINARY_CLOUD_NAME", raising=False)
    monkeypatch.delenv("CLOUDINARY_URL", raising=False)

    items_module._validate_report_image_urls([
        "http://127.0.0.1:8000/static/uploads/image.jpg"
    ])

    with pytest.raises(HTTPException) as error:
        items_module._validate_report_image_urls(["http://images.example/image.jpg"])
    assert error.value.status_code == 400

    monkeypatch.setenv("ENVIRONMENT", "production")
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

    assert reports_by_id["lost-pending"]["workflow_stage"] == 3
    assert reports_by_id["found-pending"]["workflow_stage"] == 3
    assert reports_by_id["lost-recovered"]["workflow_stage"] == 4
    assert reports_by_id["found-recovered"]["workflow_stage"] == 4
    assert reports_by_id["found-for-other-owner"]["workflow_stage"] == 4
    assert reports_by_id["lost-rejected"]["workflow_stage"] == 2
