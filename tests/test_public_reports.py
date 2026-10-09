import asyncio

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
from models import AdminMatchAlert, Base, FoundItem, LostItem, UserBlock
from routers.items import list_my_items
from schemas import MatchRequest


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


def add_test_reports(session_factory):
    with Session(session_factory.kw["bind"]) as session:
        session.add_all([
            LostItem(
                created_by="reporter-private-id",
                title="Lost blue bag",
                description="Blue bag",
                category="bags",
                lat=21.1458,
                lng=79.0882,
                report_location="Nagpur, Maharashtra",
                status="LOST",
            ),
            FoundItem(
                created_by="another-private-id",
                title="Found keys",
                description="Keys found near the station",
                category="keys",
                lat=19.0760,
                lng=72.8777,
                report_location="Mumbai, Maharashtra",
            ),
            LostItem(
                created_by="reporter-private-id",
                title="Recovered phone",
                description="Phone was recovered",
                category="electronics",
                lat=21.1458,
                lng=79.0882,
                report_location="Nagpur, Maharashtra",
                status="RECOVERED",
            ),
        ])
        session.commit()


def test_public_reports_returns_only_active_lost_and_found_reports(reports_client):
    client, session_factory = reports_client
    add_test_reports(session_factory)

    response = client.get("/api/reports")

    assert response.status_code == 200
    reports = response.json()
    assert {report["title"] for report in reports} == {"Lost blue bag", "Found keys"}
    assert all("created_by" not in report for report in reports)


def test_content_reports_can_be_submitted_reviewed_and_hidden(reports_client, monkeypatch):
    client, session_factory = reports_client
    with Session(session_factory.kw["bind"]) as session:
        item = FoundItem(
            created_by="owner",
            title="Found wallet",
            description="Brown wallet near the library",
            category="accessories",
            report_location="Nagpur",
            lat=21.1458,
            lng=79.0882,
        )
        session.add(item)
        session.commit()
        item_id = item.id

    monkeypatch.setitem(
        main.app.dependency_overrides,
        items_module.get_current_user,
        lambda: "reporter",
    )
    body = {"reason": "personal_information", "details": "Contains a phone number"}
    response = client.post(f"/api/items/found/{item_id}/reports", json=body)
    assert response.status_code == 201
    assert response.json() == {"status": "received"}
    assert client.post(f"/api/items/found/{item_id}/reports", json=body).status_code == 409

    monkeypatch.setitem(
        main.app.dependency_overrides,
        admin_module.require_admin,
        lambda: "admin",
    )
    pending = client.get("/api/admin/content-reports")
    assert pending.status_code == 200
    assert len(pending.json()) == 1
    content_report_id = pending.json()[0]["id"]

    review = client.post(
        f"/api/admin/content-reports/{content_report_id}/review",
        json={"decision": "hide"},
    )
    assert review.status_code == 200
    assert review.json() == {"status": "action_taken"}
    public_reports = client.get("/api/reports")
    assert public_reports.status_code == 200
    assert public_reports.json() == []


def test_users_cannot_report_their_own_content(reports_client, monkeypatch):
    client, session_factory = reports_client
    with Session(session_factory.kw["bind"]) as session:
        item = FoundItem(
            created_by="owner",
            title="Found wallet",
            description="Brown wallet",
            category="accessories",
            lat=21.1458,
            lng=79.0882,
        )
        session.add(item)
        session.commit()
        item_id = item.id

    monkeypatch.setitem(
        main.app.dependency_overrides,
        items_module.get_current_user,
        lambda: "owner",
    )
    response = client.post(
        f"/api/items/found/{item_id}/reports",
        json={"reason": "spam"},
    )
    assert response.status_code == 400


def test_public_reports_filters_by_city_case_insensitively(reports_client):
    client, session_factory = reports_client
    add_test_reports(session_factory)

    response = client.get("/api/reports", params={"city": "nAgPuR"})

    assert response.status_code == 200
    assert [report["title"] for report in response.json()] == ["Lost blue bag"]


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


def test_users_can_block_report_authors_and_unblock_them(reports_client, monkeypatch):
    client, session_factory = reports_client
    with Session(session_factory.kw["bind"]) as session:
        item = FoundItem(
            created_by="blocked-author",
            title="Found blue umbrella",
            description="Blue umbrella near the station",
            category="accessories",
            lat=19.0760,
            lng=72.8777,
        )
        reader_item = FoundItem(
            created_by="reader",
            title="Found red backpack",
            description="Red backpack near the park",
            category="accessories",
            lat=19.0760,
            lng=72.8777,
        )
        session.add_all([item, reader_item])
        session.commit()
        item_id = item.id

    monkeypatch.setitem(
        main.app.dependency_overrides,
        items_module.get_optional_current_user,
        lambda: "reader",
    )
    monkeypatch.setitem(
        main.app.dependency_overrides,
        users_module.get_current_user,
        lambda: "reader",
    )

    block = client.post(f"/api/users/blocked-users/found/{item_id}")
    assert block.status_code == 201
    assert block.json() == {"status": "blocked"}
    assert client.get("/api/users/blocked-users").json() == ["blocked-author"]
    assert [report["title"] for report in client.get("/api/reports").json()] == [
        "Found red backpack"
    ]

    monkeypatch.setitem(
        main.app.dependency_overrides,
        items_module.get_optional_current_user,
        lambda: "blocked-author",
    )
    assert [report["title"] for report in client.get("/api/reports").json()] == [
        "Found blue umbrella"
    ]
    monkeypatch.setitem(
        main.app.dependency_overrides,
        items_module.get_optional_current_user,
        lambda: "reader",
    )

    unblock = client.delete("/api/users/blocked-users/blocked-author")
    assert unblock.status_code == 200
    assert unblock.json() == {"status": "unblocked"}
    assert client.get("/api/users/blocked-users").json() == []
    assert len(client.get("/api/reports").json()) == 2


def test_users_cannot_block_themselves(reports_client, monkeypatch):
    client, session_factory = reports_client
    with Session(session_factory.kw["bind"]) as session:
        item = FoundItem(
            created_by="reader",
            title="Found blue umbrella",
            description="Blue umbrella near the station",
            category="accessories",
            lat=19.0760,
            lng=72.8777,
        )
        session.add(item)
        session.commit()
        item_id = item.id

    monkeypatch.setitem(
        main.app.dependency_overrides,
        users_module.get_current_user,
        lambda: "reader",
    )

    response = client.post(f"/api/users/blocked-users/found/{item_id}")

    assert response.status_code == 400
    assert response.json()["detail"] == "You cannot block yourself"


@pytest.mark.asyncio
async def test_image_matches_exclude_reports_involved_in_a_block(reports_client, monkeypatch):
    _, session_factory = reports_client
    with Session(session_factory.kw["bind"]) as session:
        query_item = FoundItem(
            created_by="reader",
            title="Found wallet",
            description="Found near station",
            category="accessories",
            lat=19.0760,
            lng=72.8777,
            image_url="https://res.cloudinary.com/fendly/image/upload/query.jpg",
        )
        candidate = LostItem(
            created_by="blocked-author",
            title="Lost wallet",
            description="Lost near station",
            category="accessories",
            lat=19.0760,
            lng=72.8777,
        )
        session.add_all([
            query_item,
            candidate,
            UserBlock(blocker_uid="reader", blocked_uid="blocked-author"),
        ])
        session.commit()

        async def fake_image_matches(_image_url, _target_type, _session):
            return [{"item": candidate, "score": 0.99, "matchType": "IMAGE"}]

        monkeypatch.setattr(items_module, "_find_cloudinary_image_matches", fake_image_matches)
        result = await items_module.match_items(
            MatchRequest(
                imageUrl=query_item.image_url,
                targetType="lost",
            ),
            session=session,
            uid="reader",
        )

    assert result == []


def test_public_reports_city_filter_excludes_coordinates_outside_india(reports_client):
    client, session_factory = reports_client
    with Session(session_factory.kw["bind"]) as session:
        session.add_all([
            FoundItem(
                created_by="reporter-in-india",
                title="Found item in Delhi",
                description="Found in Delhi",
                category="other",
                lat=28.6139,
                lng=77.2090,
                report_location="Delhi, India",
            ),
            FoundItem(
                created_by="reporter-outside-india",
                title="Found item outside India",
                description="Found outside India",
                category="other",
                lat=27.7172,
                lng=85.3240,
                report_location="Delhi, Nepal",
            ),
            FoundItem(
                created_by="reporter-without-location",
                title="Found item without Indian coordinates",
                description="Found without Indian coordinates",
                category="other",
                lat=0,
                lng=0,
                report_location="Delhi",
            ),
        ])
        session.commit()

    response = client.get("/api/reports", params={"city": "Delhi"})

    assert response.status_code == 200
    assert [report["title"] for report in response.json()] == ["Found item in Delhi"]


def test_public_reports_escapes_like_wildcards_in_city_filter(reports_client):
    client, session_factory = reports_client
    add_test_reports(session_factory)

    response = client.get("/api/reports", params={"city": "%"})

    assert response.status_code == 200
    assert response.json() == []


def test_public_reports_rejects_city_filter_over_max_length(reports_client):
    client, _ = reports_client

    response = client.get("/api/reports", params={"city": "x" * 121})

    assert response.status_code == 422


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
