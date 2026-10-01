from types import SimpleNamespace
from unittest.mock import Mock, patch

import pytest
from sqlalchemy import create_engine, inspect, text
from sqlalchemy.orm import Session

import main
from models import FoundItem, LostItem
from routers import items as items_module
from routers.items import match_items
from schemas import MatchRequest


@pytest.mark.asyncio
async def test_exact_imei_match_uses_sqlalchemy_session_and_masks_imei():
    record = LostItem(
        id="lost-item-1",
        created_by="user-1",
        title="Test phone",
        description="Lost phone",
        category="electronics",
        lat=19.076,
        lng=72.8777,
        imei="490154203237518",
    )
    session = Mock(spec=Session)
    session.scalars.return_value.all.return_value = [record]

    results = await match_items(
        MatchRequest(imei="490154203237518", targetType="lost"),
        session=session,
        _="test-user",
    )

    assert session.scalars.call_count == 1
    assert results[0]["matchType"] == "EXACT_IMEI"
    assert results[0]["score"] == 1.0
    assert results[0]["item"]["imei"] == "490154******518"


@pytest.mark.asyncio
async def test_notify_admin_of_match_uses_render_email_env_names(monkeypatch):
    monkeypatch.setenv("ADMIN_EMAIL", "admin@example.com")
    monkeypatch.setenv("SMTP_HOST", "smtp.example.com")
    monkeypatch.setenv("SMTP_USERNAME", "mailer@example.com")
    monkeypatch.setenv("SMTP_PASSWORD", "secret")
    monkeypatch.delenv("SMTP_USER", raising=False)

    captured = {}

    class DummySMTP:
        def __init__(self, host, port, timeout):
            captured["host"] = host
            captured["port"] = port
            captured["timeout"] = timeout

        def __enter__(self):
            return self

        def __exit__(self, exc_type, exc, tb):
            return False

        def starttls(self):
            captured["starttls"] = True

        def login(self, username, password):
            captured["login"] = (username, password)

        def send_message(self, message):
            captured["to"] = message["To"]
            captured["subject"] = message["Subject"]

    with patch.object(items_module.smtplib, "SMTP", DummySMTP):
        await items_module.notify_admin_of_match(
            session=Mock(spec=Session),
            query_identifier="490154203237518",
            matched_items=[{"id": "x"}],
            match_type="EXACT_IMEI",
        )

    assert captured["host"] == "smtp.example.com"
    assert captured["timeout"] == 10
    assert captured["login"] == ("mailer@example.com", "secret")
    assert captured["to"] == "admin@example.com"


@pytest.mark.asyncio
async def test_startup_adds_imei_columns_to_existing_item_tables(monkeypatch):
    engine = create_engine("sqlite://")
    with engine.begin() as connection:
        connection.execute(text("CREATE TABLE lost_items (id VARCHAR(36) PRIMARY KEY)"))
        connection.execute(text("CREATE TABLE found_items (id VARCHAR(36) PRIMARY KEY)"))

    monkeypatch.setattr(main, "engine", engine)
    monkeypatch.setenv("ENVIRONMENT", "development")

    async with main.lifespan(None):
        pass

    for table_name in ("lost_items", "found_items"):
        columns = {column["name"] for column in inspect(engine).get_columns(table_name)}
        assert "imei" in columns
    engine.dispose()


@pytest.mark.asyncio
async def test_exact_imei_match_triggers_admin_alert(monkeypatch):
    lost_item = LostItem(
        id="lost-item-1",
        created_by="user-1",
        title="Lost phone",
        description="Lost phone",
        category="electronics",
        lat=19.076,
        lng=72.8777,
        imei="490154203237518",
    )
    found_item = FoundItem(
        id="found-item-1",
        created_by="user-2",
        title="Found phone",
        description="Found phone",
        category="electronics",
        lat=19.076,
        lng=72.8777,
        imei="490154203237518",
    )
    session = Mock(spec=Session)
    session.scalars.return_value.all.return_value = [lost_item]
    alert_calls = []

    async def fake_notify(*args, **kwargs):
        alert_calls.append((args, kwargs))

    monkeypatch.setattr(
        items_module,
        "notify_admin_of_match",
        fake_notify,
    )

    results = await match_items(
        MatchRequest(imei="490154203237518", targetType="lost"),
        session=session,
        _="test-user",
    )

    assert results[0]["matchType"] == "EXACT_IMEI"
    assert results[0]["score"] == 1.0
    assert alert_calls
    assert alert_calls[0][0][1] == "490154203237518"