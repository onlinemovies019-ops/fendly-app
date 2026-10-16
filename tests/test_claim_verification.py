import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

import database
import main
from models import Base, FoundItem, LostItem, ReportClaim
from routers import admin as admin_module
from routers import items as items_module
from schemas import VisualSearchResponse


@pytest.fixture
def claim_client(monkeypatch):
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

    monkeypatch.setenv("APP_SECRET_KEY", "test-report-claim-secret-012345")
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "trusted-admin")
    monkeypatch.setenv("ADMIN_EMAIL", "")
    main.app.dependency_overrides[database.get_db] = override_get_db
    main.app.dependency_overrides[items_module.get_current_user] = lambda: "claimant"
    main.app.dependency_overrides[admin_module.require_admin] = lambda: "admin"
    client = TestClient(main.app)
    with Session(engine) as session:
        session.add_all([
            LostItem(
                id="lost-report",
                created_by="claimant",
                title="Lost camera",
                description="Black camera",
                lat=1,
                lng=1,
            ),
            LostItem(
                id="another-users-report",
                created_by="someone-else",
                title="Lost watch",
                description="Silver watch",
                lat=2,
                lng=2,
            ),
            FoundItem(
                id="found-report",
                created_by="finder",
                title="Found camera",
                description="Camera",
                lat=1,
                lng=1,
                hidden_verification_detail="Serial is ABC-123",
            ),
        ])
        session.commit()
    yield client, session_factory
    client.close()
    main.app.dependency_overrides.pop(database.get_db, None)
    main.app.dependency_overrides.pop(items_module.get_current_user, None)
    main.app.dependency_overrides.pop(admin_module.require_admin, None)
    engine.dispose()


def test_claim_submission_is_owned_and_does_not_echo_private_details(claim_client):
    client, _ = claim_client
    response = client.post(
        "/api/items/lost/lost-report/claims",
        json={
            "found_item_id": "found-report",
            "verification_detail": "Serial is ABC-123",
        },
    )

    assert response.status_code == 201
    assert response.json()["status"] == "pending"
    assert "verification_detail" not in response.json()
    assert "ABC-123" not in response.text

    denied = client.post(
        "/api/items/lost/another-users-report/claims",
        json={
            "found_item_id": "found-report",
            "verification_detail": "Serial is ABC-123",
        },
    )
    assert denied.status_code == 403


def test_hidden_verification_detail_is_only_available_to_admin_routes(claim_client):
    client, _ = claim_client
    claim_response = client.post(
        "/api/items/lost/lost-report/claims",
        json={
            "found_item_id": "found-report",
            "verification_detail": "Serial is ABC-123",
        },
    )
    assert claim_response.status_code == 201

    admin_detail = client.get(
        "/api/admin/items/found/found-report/verification-detail"
    )
    admin_claims = client.get("/api/admin/claims?status=pending")
    user_reports = client.get("/api/items/mine")

    assert admin_detail.status_code == 200
    assert admin_detail.json() == {"detail": "Serial is ABC-123"}
    assert admin_claims.status_code == 200
    assert admin_claims.json()[0]["details_match"] is True
    assert "hidden_verification_detail" not in user_reports.text
    assert "verification_detail" not in user_reports.text


def test_possible_match_preview_contains_claim_id_but_not_verification_secret():
    response = VisualSearchResponse.model_validate({
        "matches": [{
            "report_id": "found-report",
            "item": {
                "title": "Found camera",
                "category": "Electronics",
                "report_type": "FOUND",
                "hidden_verification_detail": "Serial is ABC-123",
            },
            "score": 0.91,
        }],
        "searched_reports": 1,
        "unindexed_reports": 0,
    })

    preview = response.model_dump_json()
    assert "found-report" in preview
    assert "hidden_verification_detail" not in preview
    assert "ABC-123" not in preview


def test_verification_secret_endpoint_requires_an_admin(claim_client):
    client, _ = claim_client
    main.app.dependency_overrides.pop(admin_module.require_admin)
    try:
        response = client.get(
            "/api/admin/items/found/found-report/verification-detail"
        )
    finally:
        main.app.dependency_overrides[admin_module.require_admin] = lambda: "admin"

    assert response.status_code == 403


def test_admin_cannot_confirm_claim_with_nonmatching_details(claim_client):
    client, session_factory = claim_client
    with Session(session_factory.kw["bind"]) as session:
        session.add(ReportClaim(
            id="claim-mismatch",
            claimant_uid="claimant",
            lost_item_id="lost-report",
            found_item_id="found-report",
            verification_detail="Wrong serial",
        ))
        session.commit()

    response = client.post(
        "/api/admin/claims/claim-mismatch/review",
        json={"decision": "confirmed", "review_notes": "Checked item proof"},
    )

    assert response.status_code == 400
    assert response.json()["detail"] == (
        "Submitted details do not match the administrator verification detail"
    )
