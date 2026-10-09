import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, inspect
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

import database
import main
import routers.items as items_module
from imei_security import imei_digest
from models import Base, LostItem
from routers.items import _save_item
from schemas import ItemCreate, ItemResponse


@pytest.fixture
def imei_client(monkeypatch):
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    session_factory = sessionmaker(bind=engine, autoflush=False, autocommit=False)
    sessions = []

    def override_get_db():
        session = session_factory()
        sessions.append(session)
        try:
            yield session
        finally:
            session.close()

    monkeypatch.setenv("APP_SECRET_KEY", "test-imei-secret-0123456789abcdef")
    main.app.dependency_overrides[database.get_db] = override_get_db
    client = TestClient(main.app)
    yield client, session_factory
    client.close()
    main.app.dependency_overrides.pop(database.get_db, None)
    for session in sessions:
        session.close()
    engine.dispose()


@pytest.fixture
def imei_db_session():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    with Session(engine) as session:
        yield session
    engine.dispose()


def _lost_item(imei_hash: str, *, status: str = "LOST") -> LostItem:
    return LostItem(
        created_by="reporter-1",
        title="Phone",
        description="Lost phone",
        lat=0,
        lng=0,
        status=status,
        imei_hash=imei_hash,
    )


@pytest.mark.parametrize("imei", ["123", "12345678901234", "1234567890123456", "12345678901234a"])
def test_safe_trade_rejects_invalid_imei_with_400(imei_client, imei):
    client, _ = imei_client

    response = client.get(f"/api/v1/imei/verify/{imei}")

    assert response.status_code == 400
    assert response.json()["detail"] == "IMEI must contain exactly 15 digits"


def test_safe_trade_flags_only_active_lost_reports(imei_client):
    client, session_factory = imei_client
    imei = "490154203237518"
    with Session(session_factory.kw["bind"]) as session:
        session.add(_lost_item(imei_digest(imei)))
        session.add(_lost_item(imei_digest("356938035643809"), status="RECOVERED"))
        session.commit()

    flagged = client.get(f"/api/v1/imei/verify/{imei}")
    clean = client.get("/api/v1/imei/verify/356938035643809")

    assert flagged.status_code == 200
    assert flagged.json() == {
        "status": "FLAGGED",
        "message": "This device is currently reported missing. Do not complete purchase.",
        "is_flagged": True,
    }
    assert clean.status_code == 200
    assert clean.json() == {
        "status": "CLEAN",
        "message": "No active loss reports were found for this device.",
        "is_flagged": False,
    }
    assert flagged.headers["Cache-Control"] == "no-store"


def test_safe_trade_public_lookup_is_rate_limited(imei_client):
    client, _ = imei_client

    responses = [client.get("/api/v1/imei/verify/490154203237518") for _ in range(11)]

    assert [response.status_code for response in responses] == [200] * 10 + [429]
    assert int(responses[-1].headers["Retry-After"]) > 0


def test_safe_trade_index_uses_status_and_imei_hash(imei_client):
    _, session_factory = imei_client
    indexes = inspect(session_factory.kw["bind"]).get_indexes("lost_items")

    assert any(
        index["name"] == "lost_items_status_imei_hash_idx"
        and index["column_names"] == ["status", "imei_hash"]
        for index in indexes
    )


@pytest.mark.asyncio
async def test_lost_report_imei_is_hashed_and_never_serialized(monkeypatch, imei_db_session):
    imei = "490154203237518"
    monkeypatch.setenv("APP_SECRET_KEY", "test-imei-secret-0123456789abcdef")
    monkeypatch.setattr(items_module, "require_lost_report_entitlement", lambda *_: None)

    async def no_moderation(*_):
        return None

    async def no_embedding(*_):
        return None

    async def no_image_embedding(*_):
        return None

    async def no_translation(*_, **__):
        return None

    monkeypatch.setattr(items_module, "moderate_content", no_moderation)
    monkeypatch.setattr(items_module, "create_embedding", no_embedding)
    monkeypatch.setattr(items_module, "create_image_embedding", no_image_embedding)
    monkeypatch.setattr(items_module, "translate_report_fields", no_translation)

    record = await _save_item(
        ItemCreate(
            title="Phone",
            description="Lost phone",
            community_guidelines_accepted=True,
            payment_id="test_bypass",
            imei_number=imei,
        ),
        imei_db_session,
        "reporter-1",
        LostItem,
    )
    serialized = ItemResponse.model_validate(record).model_dump()

    assert record.imei_hash == imei_digest(imei)
    assert record.imei_hash != imei
    assert "imei_number" not in serialized
    assert "imei_hash" not in serialized
    assert record.status == "LOST"
