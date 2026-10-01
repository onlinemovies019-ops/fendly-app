import asyncio
import os
from datetime import datetime, timezone
from types import SimpleNamespace
from unittest.mock import Mock, patch

import pytest
from sqlalchemy import create_engine, inspect, text
from sqlalchemy.orm import Session

import main
from models import FoundItem, LostItem, User
from routers import admin as admin_module
from routers import items as items_module
from routers.items import match_items
from schemas import ItemCreate, ItemUpdate, MatchRequest
from translation import translate_report_fields


@pytest.mark.asyncio
async def test_english_report_translation_returns_original_fields_without_api_call(monkeypatch):
    monkeypatch.delenv("OPENAI_API_KEY", raising=False)
    fields = {
        "title": "Blue backpack",
        "description": "Left at the bus stop",
        "report_location": "Central bus station",
        "category": "electronics",
    }

    translated = await translate_report_fields(
        fields["title"],
        fields["description"],
        fields["report_location"],
        category=fields["category"],
        source_language="en-US",
    )

    assert translated == fields


@pytest.mark.asyncio
async def test_non_english_report_translation_uses_openai_response(monkeypatch):
    import json

    import translation

    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    calls = {}

    class FakeResponse:
        def raise_for_status(self):
            pass

        def json(self):
            return {
                "choices": [{
                    "message": {
                        "content": json.dumps({
                            "title": "Blue bag",
                            "description": "A bag was lost at the bus station",
                            "report_location": "Central bus station",
                            "category": "bag",
                        })
                    }
                }]
            }

    class FakeAsyncClient:
        def __init__(self, **kwargs):
            calls["timeout"] = kwargs["timeout"]

        async def __aenter__(self):
            return self

        async def __aexit__(self, *args):
            pass

        async def post(self, endpoint, **kwargs):
            calls["endpoint"] = endpoint
            calls["payload"] = kwargs["json"]
            return FakeResponse()

    monkeypatch.setattr(translation.httpx, "AsyncClient", FakeAsyncClient)
    translated = await translate_report_fields(
        "निळी पिशवी",
        "बस स्थानकावर पिशवी हरवली",
        "मध्यवर्ती बस स्थानक",
        category="पिशवी",
        source_language="mr",
    )

    assert translated == {
        "title": "Blue bag",
        "description": "A bag was lost at the bus station",
        "report_location": "Central bus station",
        "category": "bag",
    }
    assert calls["payload"]["messages"][1]["content"].find('"source_language": "mr"') >= 0


@pytest.mark.asyncio
async def test_non_english_report_translation_returns_unavailable_without_key(monkeypatch):
    monkeypatch.delenv("OPENAI_API_KEY", raising=False)

    translated = await translate_report_fields("पिशवी", "हरवली", None, category="पिशवी", source_language="mr")

    assert translated is None


@pytest.mark.asyncio
async def test_report_creation_keeps_original_and_stores_english_fields(monkeypatch):
    async def no_moderation(title, description):
        return None

    async def no_embedding(text):
        return [0.0] * 1536

    async def no_image_embedding(image_url):
        return None

    async def translated_fields(title, description, report_location, category="other", source_language=None):
        assert source_language == "mr"
        return {
            "title": "Blue bag",
            "description": "A bag lost at the station",
            "report_location": "Central station",
            "category": "Bag",
        }

    monkeypatch.setattr(items_module, "moderate_content", no_moderation)
    monkeypatch.setattr(items_module, "create_embedding", no_embedding)
    monkeypatch.setattr(items_module, "create_image_embedding", no_image_embedding)
    monkeypatch.setattr(items_module, "translate_report_fields", translated_fields)
    session = Mock(spec=Session)
    payload = ItemCreate(
        title="निळी पिशवी",
        description="स्थानकावर पिशवी हरवली",
        report_location="मध्यवर्ती स्थानक",
        category="पिशवी",
        source_language="mr",
    )

    record = await items_module._save_item(payload, session, "user-1", FoundItem)

    assert record.title == "निळी पिशवी"
    assert record.description == "स्थानकावर पिशवी हरवली"
    assert record.title_en == "Blue bag"
    assert record.description_en == "A bag lost at the station"
    assert record.report_location_en == "Central station"
    assert record.category_en == "Bag"
    session.add.assert_called_once_with(record)


@pytest.mark.asyncio
async def test_admin_item_list_returns_english_and_lazily_translates_legacy_report(monkeypatch):
    legacy_report = LostItem(
        id="lost-1",
        created_by="user-1",
        title="निळी पिशवी",
        description="स्थानकावर पिशवी हरवली",
        category="पिशवी",
        lat=0,
        lng=0,
    )
    session = Mock(spec=Session)
    session.scalars.side_effect = [
        SimpleNamespace(all=lambda: [legacy_report]),
        SimpleNamespace(all=lambda: []),
    ]

    async def translated_fields(title, description, report_location, category="other", source_language=None):
        return {
            "title": "Blue bag",
            "description": "A bag lost at the station",
            "report_location": "",
            "category": "Bag",
        }

    monkeypatch.setattr(admin_module, "translate_report_fields", translated_fields)
    results = await admin_module.list_all_items(session=session, _="admin-1")

    assert results[0]["title"] == "Blue bag"
    assert results[0]["description"] == "A bag lost at the station"
    assert results[0]["category"] == "Bag"
    assert results[0]["translation_available"] is True
    assert legacy_report.title == "निळी पिशवी"
    session.commit.assert_called_once()


@pytest.mark.asyncio
async def test_report_update_refreshes_english_fields(monkeypatch):
    report = LostItem(
        id="lost-1",
        created_by="user-1",
        title="जुनी पिशवी",
        description="जुने वर्णन",
        category="other",
        lat=0,
        lng=0,
        edit_count=0,
        created_at=datetime.now(timezone.utc),
    )
    session = Mock(spec=Session)
    session.scalar.return_value = report

    async def no_moderation(title, description):
        return None

    async def translated_fields(title, description, report_location, category="other", source_language=None):
        return {
            "title": "New bag",
            "description": "New description",
            "report_location": "Central station",
            "category": "Bag",
        }

    monkeypatch.setattr(items_module, "moderate_content", no_moderation)
    monkeypatch.setattr(items_module, "translate_report_fields", translated_fields)
    payload = ItemUpdate(
        title="नवी पिशवी",
        description="नवीन वर्णन",
        report_location="मध्यवर्ती स्थानक",
        category="पिशवी",
        source_language="mr",
    )

    updated = await items_module.update_item("lost", "lost-1", payload, session=session, uid="user-1")

    assert updated.title == "नवी पिशवी"
    assert updated.title_en == "New bag"
    assert updated.description_en == "New description"
    assert updated.report_location_en == "Central station"
    assert updated.category_en == "Bag"
    assert updated.source_language == "mr"


@pytest.mark.asyncio
async def test_admin_user_search_returns_translated_report_fields():
    report = FoundItem(
        id="found-1",
        created_by="user-1",
        title="निळी पिशवी",
        description="स्थानकावर सापडली",
        title_en="Blue bag",
        description_en="Found at the station",
        report_location_en="Central station",
        category_en="Bag",
        category="पिशवी",
        lat=0,
        lng=0,
    )
    user = User(firebase_uid="user-1", username="sample-user", full_name="Sample User")
    session = Mock(spec=Session)
    session.scalars.side_effect = [
        SimpleNamespace(all=lambda: [user]),
        SimpleNamespace(all=lambda: [report]),
        SimpleNamespace(all=lambda: []),
    ]

    results = await admin_module.search_users_and_reports(q="sample", session=session, _="admin-1")

    assert results[0]["reports"][0]["title"] == "Blue bag"
    assert results[0]["reports"][0]["description"] == "Found at the station"
    assert results[0]["reports"][0]["category"] == "Bag"


@pytest.mark.asyncio
async def test_admin_alert_titles_and_reason_use_english_report_text():
    found_item = FoundItem(
        id="found-1",
        created_by="user-1",
        title="सापडलेली पिशवी",
        description="वर्णन",
        title_en="Found bag",
        description_en="Description",
        report_location_en="",
        category_en="Bag",
        category="bag",
        lat=0,
        lng=0,
    )
    lost_item = LostItem(
        id="lost-1",
        created_by="user-2",
        title="हरवलेली पिशवी",
        description="वर्णन",
        title_en="Lost bag",
        description_en="Description",
        report_location_en="",
        category_en="Bag",
        category="bag",
        lat=0,
        lng=0,
    )
    session = Mock(spec=Session)
    session.get.side_effect = [found_item, lost_item]
    alerts = [{"found_item_id": "found-1", "lost_item_id": "lost-1", "reason": "original-language text"}]

    translated_alerts = await admin_module._translate_alert_titles(alerts, session)

    assert translated_alerts[0]["found_title"] == "Found bag"
    assert translated_alerts[0]["lost_title"] == "Lost bag"
    assert translated_alerts[0]["reason"] == "Found 'Found bag' may match lost report 'Lost bag'."


def test_admin_uid_parser_accepts_json_and_newline_lists(monkeypatch):
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", '["uid-1", "uid-2"]\nuid-3\n')
    parsed = admin_module._parse_admin_uids(os.getenv("ADMIN_FIREBASE_UIDS", ""))
    assert parsed == {"uid-1", "uid-2", "uid-3"}


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
async def test_notify_admin_of_match_persists_dashboard_alert(monkeypatch):
    monkeypatch.setenv("ADMIN_EMAIL", "admin@example.com")
    monkeypatch.setenv("BREVO_API_KEY", "brevo-key")
    monkeypatch.setenv("SENDER_EMAIL", "noreply@example.com")
    monkeypatch.delenv("SMTP_HOST", raising=False)
    monkeypatch.delenv("SMTP_USERNAME", raising=False)
    monkeypatch.delenv("SMTP_PASSWORD", raising=False)

    calls = {}

    def fake_persist(session, found_item, lost_item, confidence):
        calls["payload"] = (found_item.id, lost_item.id, confidence)
        return True

    def fake_send_admin_match_email(item_title, match_score, match_details):
        return True

    with patch.object(items_module, "persist_admin_match_alert", fake_persist), patch.object(items_module, "send_admin_match_email", fake_send_admin_match_email), patch.object(items_module.asyncio, "create_task", lambda coro: coro.close() or object()):
        await items_module.notify_admin_of_match(
            session=Mock(spec=Session),
            query_identifier="490154203237518",
            matched_items=[LostItem(id="lost-item-1", title="Test phone", created_by="u1", description="Lost phone", category="electronics", lat=0, lng=0)],
            match_type="EXACT_IMEI",
        )

    assert calls["payload"] == ("lost-item-1", "lost-item-1", 1.0)


@pytest.mark.asyncio
async def test_notify_admin_of_match_uses_render_email_env_names(monkeypatch):
    monkeypatch.setenv("ADMIN_EMAIL", "admin@example.com")
    monkeypatch.setenv("BREVO_API_KEY", "brevo-key")
    monkeypatch.setenv("SENDER_EMAIL", "noreply@example.com")
    monkeypatch.delenv("SMTP_HOST", raising=False)
    monkeypatch.delenv("SMTP_USERNAME", raising=False)
    monkeypatch.delenv("SMTP_PASSWORD", raising=False)

    sent = {}

    def fake_send_admin_match_email(item_title, match_score, match_details):
        sent["title"] = item_title
        sent["score"] = match_score
        sent["details"] = match_details
        return True

    scheduled_task = None

    def fake_create_task(coro):
        nonlocal scheduled_task
        scheduled_task = asyncio.get_running_loop().create_task(coro)
        return scheduled_task

    with patch.object(items_module, "send_admin_match_email", fake_send_admin_match_email), patch.object(items_module.asyncio, "create_task", fake_create_task):
        await items_module.notify_admin_of_match(
            session=Mock(spec=Session),
            query_identifier="490154203237518",
            matched_items=[{"id": "x"}],
            match_type="EXACT_IMEI",
        )
        await scheduled_task

    assert sent["title"].startswith("EXACT_IMEI")
    assert sent["score"] == 1.0
    assert "490154203237518" in sent["details"]


@pytest.mark.asyncio
async def test_notify_admin_of_match_starts_background_task_without_blocking(monkeypatch):
    monkeypatch.setenv("ADMIN_EMAIL", "admin@example.com")
    monkeypatch.setenv("SMTP_HOST", "smtp.example.com")
    monkeypatch.setenv("SMTP_USERNAME", "mailer@example.com")
    monkeypatch.setenv("SMTP_PASSWORD", "secret")

    scheduled = {}

    def fake_create_task(coro):
        scheduled["coro"] = coro
        coro.close()
        return object()

    monkeypatch.setattr(items_module.asyncio, "create_task", fake_create_task)

    await items_module.notify_admin_of_match(
        session=Mock(spec=Session),
        query_identifier="490154203237518",
        matched_items=[{"id": "x"}],
        match_type="EXACT_IMEI",
    )

    assert "coro" in scheduled


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
        assert {"title_en", "description_en", "report_location_en", "category_en", "source_language"} <= columns
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