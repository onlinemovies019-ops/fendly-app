import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from sqlalchemy.pool import StaticPool

from models import (
    AdminMatchAlert,
    Base,
    FoundItem,
    LostItem,
    UserNotification,
)
from scripts.purge_account_deleted_reports import (
    _validate_runtime_config,
    _validate_media_delete_config,
    purge_account_deleted_reports,
)


@pytest.fixture
def cleanup_session():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    with Session(engine) as session:
        yield session


def test_purge_removes_legacy_reports_indexes_and_alerts_but_keeps_shared_media(
    cleanup_session,
):
    image_url = "https://cdn.example.test/shared.jpg"
    cleanup_session.add_all(
        [
            LostItem(
                id="legacy-lost",
                created_by="account-deleted",
                title="Private lost report",
                description="Private details",
                lat=1,
                lng=2,
                image_url=image_url,
            ),
            FoundItem(
                id="legacy-found",
                created_by="account-deleted",
                title="Private found report",
                description="Private details",
                lat=3,
                lng=4,
            ),
            LostItem(
                id="active-lost",
                created_by="active-user",
                title="Active report",
                description="Still needed",
                lat=5,
                lng=6,
                image_url=image_url,
            ),
            AdminMatchAlert(
                found_item_id="legacy-found",
                lost_item_id="legacy-lost",
                found_title="Private found report",
                lost_title="Private lost report",
                confidence=0.9,
                reason="Potential match",
            ),
            AdminMatchAlert(
                found_item_id="legacy-found",
                lost_item_id="other-lost",
                found_title="Private found report",
                lost_title="Other report",
                confidence=0.9,
                reason="Potential match",
            ),
            UserNotification(
                firebase_uid="active-user",
                found_item_id="legacy-found",
                title="Potential match",
                body="Potential match",
                score=0.9,
            ),
        ]
    )
    cleanup_session.commit()
    deleted_supabase_ids = []
    deleted_firestore_ids = []
    deleted_assets = []

    counts = purge_account_deleted_reports(
        cleanup_session,
        delete_supabase_data=lambda ids: deleted_supabase_ids.extend(ids),
        delete_firestore_reports=lambda ids: deleted_firestore_ids.extend(ids),
        delete_asset=deleted_assets.append,
    )

    assert counts == {"lost_reports": 1, "found_reports": 1}
    assert set(deleted_supabase_ids) == {"legacy-found", "legacy-lost"}
    assert set(deleted_firestore_ids) == {"legacy-found", "legacy-lost"}
    assert deleted_assets == []
    assert cleanup_session.get(LostItem, "legacy-lost") is None
    assert cleanup_session.get(FoundItem, "legacy-found") is None
    assert cleanup_session.get(LostItem, "active-lost") is not None
    assert cleanup_session.query(AdminMatchAlert).count() == 0
    assert cleanup_session.query(UserNotification).count() == 0


def test_purge_with_no_legacy_reports_does_not_touch_external_services(cleanup_session):
    calls = []

    counts = purge_account_deleted_reports(
        cleanup_session,
        delete_supabase_data=lambda ids: calls.append(("supabase", ids)),
        delete_firestore_reports=lambda ids: calls.append(("firestore", ids)),
        delete_asset=lambda url: calls.append(("asset", url)),
    )

    assert counts == {"lost_reports": 0, "found_reports": 0}
    assert calls == []


def test_cloudinary_purge_requires_server_side_delete_credentials(monkeypatch):
    monkeypatch.delenv("CLOUDINARY_CLOUD_NAME", raising=False)
    monkeypatch.delenv("CLOUDINARY_API_KEY", raising=False)
    monkeypatch.delenv("CLOUDINARY_API_SECRET", raising=False)

    with pytest.raises(SystemExit, match="CLOUDINARY_CLOUD_NAME"):
        _validate_media_delete_config(
            {"https://res.cloudinary.com/fendly/image/upload/report.jpg"}
        )


def test_cleanup_requires_production_environment(monkeypatch):
    monkeypatch.delenv("ENVIRONMENT", raising=False)

    with pytest.raises(SystemExit, match="ENVIRONMENT"):
        _validate_runtime_config(apply=False)


@pytest.mark.parametrize(
    "database_url",
    ["", "sqlite:///./fendly.db", "https://database.example.test"],
)
def test_cleanup_refuses_non_postgres_database_targets(monkeypatch, database_url):
    monkeypatch.setenv("ENVIRONMENT", "production")
    monkeypatch.setenv("DATABASE_URL", database_url)

    with pytest.raises(SystemExit, match="production PostgreSQL"):
        _validate_runtime_config(apply=False)


def test_cleanup_apply_requires_provider_credentials(monkeypatch):
    monkeypatch.setenv("ENVIRONMENT", "production")
    monkeypatch.setenv(
        "DATABASE_URL",
        "postgresql://user:password@db.example.test:5432/fendly",
    )
    monkeypatch.delenv("SUPABASE_URL", raising=False)
    monkeypatch.delenv("SUPABASE_SERVICE_ROLE_KEY", raising=False)
    monkeypatch.delenv("FIREBASE_SERVICE_ACCOUNT_JSON", raising=False)
    monkeypatch.delenv("GOOGLE_APPLICATION_CREDENTIALS", raising=False)

    with pytest.raises(SystemExit, match="SUPABASE_URL"):
        _validate_runtime_config(apply=True)
