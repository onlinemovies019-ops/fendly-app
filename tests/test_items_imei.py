import asyncio
import os
from datetime import datetime, timezone
from types import SimpleNamespace
from unittest.mock import Mock, patch

import pytest
from sqlalchemy import create_engine, inspect, text
from sqlalchemy.dialects import sqlite
from sqlalchemy import select
from sqlalchemy.orm import Session

import main
from models import FoundItem, LostItem, User
from routers import admin as admin_module
from routers import items as items_module
from routers.items import match_items
from schemas import ItemCreate, ItemUpdate, MatchRequest
from translation import translate_report_fields, translate_report_fields_batch


@pytest.mark.parametrize(("title", "description", "expected"), [
    ("Mobile phone", "Black smartphone", "Electronics"),
    ("Cat", "Small brown pet", "Animals"),
    ("Missing child", "Boy last seen near school", "People"),
    ("Wallet", "Black leather item", "Apparels and accessories"),
    ("Bicycle", "Blue cycle", "Automobile"),
    ("Unrecognized object", "No useful details", "other"),
])
def test_infer_item_category_from_report_text(title, description, expected):
    assert items_module.infer_item_category(title, description) == expected


def test_resolve_item_category_preserves_explicit_category():
    assert items_module.resolve_item_category("electronics", "Wallet", "Leather purse") == "electronics"


def test_report_title_category_takes_priority_over_description_terms():
    assert items_module.infer_item_category("Wallet", "Light-colored item") == "Apparels and accessories"


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

    monkeypatch.setenv("GEMINI_API_KEY", "test-key")
    monkeypatch.delenv("OPENAI_API_KEY", raising=False)
    calls = {}

    class FakeResponse:
        status_code = 200

        def raise_for_status(self):
            pass

        def json(self):
            return {
                "candidates": [{
                    "content": {
                        "parts": [{"text": json.dumps({
                            "title": "Blue bag",
                            "description": "A bag was lost at the bus station",
                            "report_location": "Central bus station",
                            "category": "bag",
                        })}]
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
            calls["headers"] = kwargs["headers"]
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
    assert calls["endpoint"].endswith("/models/gemini-3.8-flash:generateContent")
    assert calls["headers"]["x-goog-api-key"] == "test-key"
    assert json.loads(calls["payload"]["contents"][0]["parts"][0]["text"])["source_language"] == "mr"


@pytest.mark.asyncio
async def test_gemini_translation_retries_with_fallback_when_primary_is_overloaded(monkeypatch):
    import json

    import httpx
    import translation

    monkeypatch.setenv("GEMINI_API_KEY", "test-key")
    monkeypatch.setenv("GEMINI_TRANSLATION_MODEL", "gemini-3.8-flash")
    monkeypatch.setenv("GEMINI_TRANSLATION_FALLBACK_MODEL", "gemini-3.1-flash-lite")
    monkeypatch.delenv("OPENAI_API_KEY", raising=False)
    requested_endpoints = []

    class FakeResponse:
        def __init__(self, status_code):
            self.status_code = status_code

        def raise_for_status(self):
            if self.status_code >= 400:
                request = httpx.Request("POST", "https://example.test")
                response = httpx.Response(self.status_code, request=request)
                raise httpx.HTTPStatusError("model overloaded", request=request, response=response)

        def json(self):
            return {"candidates": [{"content": {"parts": [{"text": json.dumps({
                "title": "Blue bag",
                "description": "Lost at the station",
                "report_location": "Central station",
                "category": "Bag",
            })}]}}]}

    class FakeAsyncClient:
        def __init__(self, **kwargs):
            pass

        async def __aenter__(self):
            return self

        async def __aexit__(self, *args):
            pass

        async def post(self, endpoint, **kwargs):
            requested_endpoints.append(endpoint)
            return FakeResponse(503 if len(requested_endpoints) == 1 else 200)

    monkeypatch.setattr(translation.httpx, "AsyncClient", FakeAsyncClient)
    translated = await translate_report_fields("निळी पिशवी", "हरवली", "मध्यवर्ती स्थानक", "पिशवी", "mr")

    assert translated["title"] == "Blue bag"
    assert requested_endpoints[0].endswith("/models/gemini-3.8-flash:generateContent")
    assert requested_endpoints[1].endswith("/models/gemini-3.1-flash-lite:generateContent")


@pytest.mark.asyncio
async def test_non_english_report_translation_returns_unavailable_without_key(monkeypatch):
    monkeypatch.delenv("OPENAI_API_KEY", raising=False)
    monkeypatch.delenv("GEMINI_API_KEY", raising=False)

    translated = await translate_report_fields("पिशवी", "हरवली", None, category="पिशवी", source_language="mr")

    assert translated is None


@pytest.mark.asyncio
async def test_batch_translation_preserves_report_ids_and_passes_english_through(monkeypatch):
    import json

    import translation

    request_payload = {}

    async def fake_generate(user_content):
        request_payload.update(json.loads(user_content))
        return {
            "reports": [{
                "id": "lost_items:mr-1",
                "title": "Blue bag",
                "description": "Lost at the station",
                "report_location": "Central station",
                "category": "Bag",
            }]
        }

    monkeypatch.setattr(translation, "_generate_json_translation", fake_generate)
    translated = await translate_report_fields_batch([
        {
            "id": "lost_items:mr-1",
            "source_language": "mr",
            "title": "निळी पिशवी",
            "description": "स्थानकावर हरवली",
            "report_location": "मध्यवर्ती स्थानक",
            "category": "पिशवी",
        },
        {
            "id": "found_items:en-1",
            "source_language": "en",
            "title": "Wallet",
            "description": "Found near the gate",
            "report_location": "",
            "category": "other",
        },
    ])

    assert request_payload["reports"][0]["id"] == "lost_items:mr-1"
    assert translated["lost_items:mr-1"]["title"] == "Blue bag"
    assert translated["found_items:en-1"]["title"] == "Wallet"


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
        image_url="https://cdn.example/one.jpg",
        image_urls=[
            "https://cdn.example/one.jpg",
            "https://cdn.example/two.jpg",
            "https://cdn.example/three.jpg",
        ],
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
    assert record.image_url == "https://cdn.example/one.jpg"
    assert record.image_urls == [
        "https://cdn.example/one.jpg",
        "https://cdn.example/two.jpg",
        "https://cdn.example/three.jpg",
    ]
    session.add.assert_called_once_with(record)


@pytest.mark.asyncio
async def test_report_creation_infers_category_from_english_translation(monkeypatch):
    embedding_inputs = []

    async def no_moderation(title, description):
        return None

    async def capture_embedding(text):
        embedding_inputs.append(text)
        return [0.0] * 1536

    async def no_image_embedding(image_url):
        return None

    async def translated_fields(title, description, report_location, category="other", source_language=None):
        return {
            "title": "Mobile phone",
            "description": "Black smartphone",
            "report_location": "",
            "category": "Other",
        }

    monkeypatch.setattr(items_module, "moderate_content", no_moderation)
    monkeypatch.setattr(items_module, "create_embedding", capture_embedding)
    monkeypatch.setattr(items_module, "create_image_embedding", no_image_embedding)
    monkeypatch.setattr(items_module, "translate_report_fields", translated_fields)
    session = Mock(spec=Session)
    payload = ItemCreate(
        title="मोबाइल वस्तु",
        description="काली वस्तु",
        category="other",
        source_language="hi",
    )

    record = await items_module._save_item(payload, session, "user-1", FoundItem)

    assert record.category == "Electronics"
    assert record.category_en == "Electronics"
    assert len(embedding_inputs) == 2
    assert "Electronics" in embedding_inputs[-1]


@pytest.mark.asyncio
async def test_admin_item_list_returns_english_and_lazily_translates_legacy_report(monkeypatch):
    legacy_report = LostItem(
        id="lost-1",
        created_by="user-1",
        title="निळी पिशवी",
        description="स्थानकावर पिशवी हरवली",
        category="पिशवी",
        image_url="https://cdn.example/one.jpg",
        image_urls=["https://cdn.example/one.jpg", "https://cdn.example/two.jpg"],
        lat=0,
        lng=0,
    )
    session = Mock(spec=Session)
    session.scalars.side_effect = [
        SimpleNamespace(all=lambda: [legacy_report]),
        SimpleNamespace(all=lambda: []),
    ]

    async def translated_batch(reports):
        return {
            report["id"]: {
                "title": "Blue bag",
                "description": "A bag lost at the station",
                "report_location": "",
                "category": "Bag",
            }
            for report in reports
        }

    monkeypatch.setattr(admin_module, "translate_report_fields_batch", translated_batch)
    results = await admin_module.list_all_items(session=session, _="admin-1")

    assert results[0]["title"] == "Blue bag"
    assert results[0]["description"] == "A bag lost at the station"
    assert results[0]["category"] == "Bag"
    assert results[0]["translation_available"] is True
    assert results[0]["image_urls"] == ["https://cdn.example/one.jpg", "https://cdn.example/two.jpg"]
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


@pytest.mark.asyncio
async def test_admin_alert_titles_fall_back_to_original_when_translation_is_unavailable(monkeypatch):
    found_item = FoundItem(
        id="found-1",
        created_by="user-1",
        title="Green wallet",
        description="A green wallet",
        lat=0,
        lng=0,
    )
    lost_item = LostItem(
        id="lost-1",
        created_by="user-2",
        title="Wallet",
        description="A wallet",
        lat=0,
        lng=0,
    )
    session = Mock(spec=Session)
    session.get.side_effect = [found_item, lost_item]

    async def skip_translation(items, session):
        return None

    monkeypatch.setattr(admin_module, "_ensure_english_translations", skip_translation)
    alerts = [{"found_item_id": "found-1", "lost_item_id": "lost-1"}]

    translated_alerts = await admin_module._translate_alert_titles(alerts, session)

    assert translated_alerts[0]["found_title"] == "Green wallet"
    assert translated_alerts[0]["lost_title"] == "Wallet"
    assert translated_alerts[0]["reason"] == "Found 'Green wallet' may match lost report 'Wallet'."


@pytest.mark.asyncio
async def test_admin_alert_preserves_stored_titles_when_linked_reports_are_not_in_sql():
    session = Mock(spec=Session)
    session.get.return_value = None
    alerts = [{
        "found_item_id": "firestore-found-1",
        "lost_item_id": "firestore-lost-1",
        "found_title": "Green wallet",
        "lost_title": "Wallet",
    }]

    translated_alerts = await admin_module._translate_alert_titles(alerts, session)

    assert translated_alerts[0]["found_title"] == "Green wallet"
    assert translated_alerts[0]["lost_title"] == "Wallet"
    assert translated_alerts[0]["reason"] == "Found 'Green wallet' may match lost report 'Wallet'."


@pytest.mark.asyncio
async def test_admin_alert_includes_indexed_images_and_descriptions(monkeypatch):
    session = Mock(spec=Session)
    session.get.return_value = None
    alerts = [{
        "found_item_id": "found-1",
        "lost_item_id": "lost-1",
        "found_title": "Green wallet",
        "lost_title": "Wallet",
    }]
    response = Mock()
    response.json.return_value = [
        {"source_id": "found-1", "title": "Green wallet", "description": "Green leather wallet", "image_url": "https://images.test/found.jpg"},
        {"source_id": "lost-1", "title": "Wallet", "description": "Lost near station", "image_url": "https://images.test/lost.jpg"},
    ]
    monkeypatch.setattr(admin_module, "_supabase_admin_alert_request", Mock(return_value=response))

    translated_alerts = await admin_module._translate_alert_titles(alerts, session)

    assert translated_alerts[0]["found_description"] == "Green leather wallet"
    assert translated_alerts[0]["lost_description"] == "Lost near station"
    assert translated_alerts[0]["found_image_url"] == "https://images.test/found.jpg"
    assert translated_alerts[0]["lost_image_url"] == "https://images.test/lost.jpg"


def test_admin_alert_review_persists_decision_and_marks_alert_read(monkeypatch):
    alert = SimpleNamespace(review_status="pending", is_read=False)
    session = Mock(spec=Session)
    session.get.return_value = alert
    monkeypatch.setattr(admin_module, "_supabase_admin_alert_request", lambda *args, **kwargs: None)

    result = admin_module.review_match_alert(
        "alert-1",
        admin_module.AlertReviewRequest(decision="confirmed"),
        session,
        "admin-1",
    )

    assert result == {"review_status": "confirmed"}
    assert alert.review_status == "confirmed"
    assert alert.is_read is True
    session.commit.assert_called_once()


def test_admin_report_search_requires_every_comma_filter_to_match_title_or_description():
    conditions = admin_module._report_search_conditions(LostItem, ["bike", "green"])
    statement = select(LostItem).where(*conditions)
    compiled = statement.compile(dialect=sqlite.dialect())

    assert str(compiled).count(" AND ") == 1
    assert "lost_items.title" in str(compiled)
    assert "lost_items.description" in str(compiled)
    assert "lost_items.title_en" in str(compiled)
    assert "lost_items.description_en" in str(compiled)
    assert "%bike%" in compiled.params.values()
    assert "%green%" in compiled.params.values()


def test_admin_test_report_filter_uses_explicit_markers_not_common_words():
    test_report = LostItem(
        id="test-report-1",
        created_by="ordinary-user",
        title="Test report: generated fixture",
        description="Sample data only",
        category="other",
        lat=0,
        lng=0,
    )
    real_report = LostItem(
        id="lost-1",
        created_by="contest-user-1",
        title="Contest bicycle",
        description="Testing the green lock before reporting",
        category="cycle",
        lat=0,
        lng=0,
    )

    assert admin_module._is_test_report(test_report) is True
    assert admin_module._is_test_report(real_report) is False


@pytest.mark.asyncio
async def test_admin_report_search_returns_matching_lost_and_found_reports_in_english():
    lost_report = LostItem(
        id="lost-bike-1",
        created_by="user-1",
        title="Bike 1234",
        description="Green bicycle",
        title_en="Bike 1234",
        description_en="Green bicycle",
        report_location_en="",
        category_en="Bicycle",
        category="cycle",
        lat=0,
        lng=0,
    )
    found_report = FoundItem(
        id="found-bike-1",
        created_by="user-2",
        title="Bike 1234 found",
        description="Green cycle",
        title_en="Bike 1234 found",
        description_en="Green cycle",
        report_location_en="",
        category_en="Bicycle",
        category="cycle",
        lat=0,
        lng=0,
    )
    session = Mock(spec=Session)
    session.scalars.side_effect = [
        SimpleNamespace(all=lambda: [lost_report]),
        SimpleNamespace(all=lambda: [found_report]),
    ]

    results = await admin_module.search_admin_reports(q="bike, 1234", session=session, _="admin-1")

    assert [item["id"] for item in results] == ["lost-bike-1", "found-bike-1"]
    assert [item["title"] for item in results] == ["Bike 1234", "Bike 1234 found"]
    assert all(item["translation_available"] for item in results)
    assert len(session.scalars.call_args_list) == 2
    for call in session.scalars.call_args_list:
        compiled = call.args[0].compile(dialect=sqlite.dialect())
        assert "%bike%" in compiled.params.values()
        assert "%1234%" in compiled.params.values()


@pytest.mark.asyncio
async def test_admin_live_report_list_keeps_reports_with_test_like_user_text():
    report = LostItem(
        id="lost-test-1",
        created_by="contest-user-1",
        title="Contest bicycle",
        description="Testing the green lock before reporting",
        title_en="Contest bicycle",
        description_en="Testing the green lock before reporting",
        report_location_en="",
        category_en="Bicycle",
        category="cycle",
        lat=0,
        lng=0,
    )
    session = Mock(spec=Session)
    session.scalars.side_effect = [
        SimpleNamespace(all=lambda: [report]),
        SimpleNamespace(all=lambda: []),
    ]

    results = await admin_module.list_all_items(session=session, _="admin-1")

    assert len(results) == 1
    assert results[0]["id"] == "lost-test-1"
    assert results[0]["title"] == "Contest bicycle"


@pytest.mark.asyncio
async def test_admin_live_report_list_hides_explicit_test_report():
    test_report = LostItem(
        id="test-report-1",
        created_by="ordinary-user",
        title="Test report: generated fixture",
        description="Sample data only",
        title_en="Test report: generated fixture",
        description_en="Sample data only",
        report_location_en="",
        category_en="Other",
        category="other",
        lat=0,
        lng=0,
    )
    real_report = LostItem(
        id="lost-1",
        created_by="contest-user-1",
        title="Contest bicycle",
        description="Testing the green lock before reporting",
        title_en="Contest bicycle",
        description_en="Testing the green lock before reporting",
        report_location_en="",
        category_en="Cycle",
        category="cycle",
        lat=0,
        lng=0,
    )
    session = Mock(spec=Session)
    session.scalars.side_effect = [
        SimpleNamespace(all=lambda: [test_report, real_report]),
        SimpleNamespace(all=lambda: []),
    ]

    results = await admin_module.list_all_items(session=session, _="admin-1")

    assert [item["id"] for item in results] == ["lost-1"]


@pytest.mark.asyncio
async def test_legacy_translation_batches_27_reports_into_three_provider_calls(monkeypatch):
    reports = [
        LostItem(
            id=f"lost-{index}",
            created_by="ordinary-user",
            title=f"हरवलेली वस्तू {index}",
            description="बस स्थानकाजवळ हरवली",
            category="other",
            lat=0,
            lng=0,
        )
        for index in range(27)
    ]
    batch_sizes = []

    async def translated_batch(batch):
        batch_sizes.append(len(batch))
        return {
            report["id"]: {
                "title": f"Lost item {index}",
                "description": "Lost near the bus station",
                "report_location": "",
                "category": "Other",
            }
            for index, report in enumerate(batch)
        }

    monkeypatch.setattr(admin_module, "translate_report_fields_batch", translated_batch)
    session = Mock(spec=Session)

    await admin_module._ensure_english_translations(reports, session)

    assert sorted(batch_sizes) == [7, 10, 10]
    assert all(report.title_en is not None for report in reports)
    session.commit.assert_called_once()


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
async def test_cloudinary_image_match_uses_internal_function_and_returns_sql_report(monkeypatch):
    candidate = LostItem(
        id="lost-image-1",
        created_by="user-1",
        title="Blue wallet",
        description="Lost near the bus stop",
        category="other",
        lat=0,
        lng=0,
    )
    session = Mock(spec=Session)
    session.scalars.return_value.all.return_value = [candidate]
    monkeypatch.setenv("IMAGE_MATCHING_FUNCTION_URL", "https://functions.example/matchReportImages")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "server-only-key")
    calls = {}

    class FakeResponse:
        def raise_for_status(self):
            pass

        def json(self):
            return {"matches": [{"source_id": "lost-image-1", "similarity": 0.91}]}

    class FakeAsyncClient:
        def __init__(self, **kwargs):
            calls["timeout"] = kwargs["timeout"]

        async def __aenter__(self):
            return self

        async def __aexit__(self, *args):
            pass

        async def post(self, endpoint, **kwargs):
            calls["endpoint"] = endpoint
            calls["request"] = kwargs
            return FakeResponse()

    monkeypatch.setattr(items_module.httpx, "AsyncClient", FakeAsyncClient)
    results = await items_module.match_items(
        MatchRequest(imageUrl="https://res.cloudinary.com/fendly/image/upload/wallet.jpg", targetType="lost"),
        session=session,
        _="user-1",
    )

    assert calls["endpoint"] == "https://functions.example/matchReportImages"
    assert calls["request"]["headers"]["Authorization"] == "Bearer server-only-key"
    assert results[0]["item"].id == "lost-image-1"
    assert results[0]["score"] == 0.91
    assert results[0]["matchType"] == "IMAGE"


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