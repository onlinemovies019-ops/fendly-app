import asyncio

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

import database
import main
from models import AdminMatchAlert, Base, FoundItem, LostItem
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


def test_public_reports_filters_by_city_case_insensitively(reports_client):
    client, session_factory = reports_client
    add_test_reports(session_factory)

    response = client.get("/api/reports", params={"city": "nAgPuR"})

    assert response.status_code == 200
    assert [report["title"] for report in response.json()] == ["Lost blue bag"]


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
