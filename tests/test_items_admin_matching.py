import asyncio
import os
from datetime import datetime, timezone
from types import SimpleNamespace
from unittest.mock import AsyncMock, Mock, patch

import pytest
from fastapi import HTTPException
from sqlalchemy import create_engine, inspect, text
from sqlalchemy.dialects import postgresql, sqlite
from sqlalchemy import select
from sqlalchemy.orm import Session

import main
from models import FoundItem, LostItem, User
from routers import admin as admin_module
from routers import items as items_module
from routers.items import match_items
from schemas import ItemCreate, ItemUpdate, MatchRequest
from translation import translate_report_fields, translate_report_fields_batch


def test_admin_report_summary_counts_reports_without_loading_report_rows():
    session = Mock(spec=Session)
    session.scalar.side_effect = [17, 23]

    summary = admin_module.get_admin_item_summary(session=session, _="admin-1")

    assert summary == {"lost": 17, "found": 23}
    assert session.scalar.call_count == 2
    for call in session.scalar.call_args_list:
        sql = str(call.args[0].compile(dialect=postgresql.dialect()))
        assert "count(" in sql.lower()
        assert "!~*" in sql


@pytest.mark.asyncio
async def test_admin_report_browse_returns_a_limited_page_without_translation(monkeypatch):
    report = LostItem(
        id="lost-page-1",
        created_by="user-1",
        title="हरवलेली पिशवी",
        description="बस स्थानकाजवळ",
        category="bag",
        lat=12.0,
        lng=77.0,
    )
    session = Mock(spec=Session)
    session.scalars.return_value.all.return_value = [report]
    translation = AsyncMock()
    monkeypatch.setattr(admin_module, "translate_report_fields_batch", translation)
    monkeypatch.setattr(
        admin_module,
        "_report_image_fields_batch",
        lambda items: {
            (item.__tablename__, item.id): {"image_url": None, "image_urls": []}
            for item in items
        },
    )

    page = await admin_module.browse_admin_items(
        "lost",
        30,
        30,
        session=session,
        _="admin-1",
    )

    assert page["offset"] == 30
    assert page["limit"] == 30
    assert page["has_more"] is False
    assert page["items"][0]["title"] == "हरवलेली पिशवी"
    assert page["items"][0]["description"] == "बस स्थानकाजवळ"
    sql = str(session.scalars.call_args.args[0].compile(dialect=postgresql.dialect()))
    assert "LIMIT" in sql
    assert "OFFSET" in sql
    assert "!~*" in sql
    translation.assert_not_awaited()


@pytest.mark.asyncio
async def test_report_creation_requires_community_guidelines_acceptance():
    payload = ItemCreate(title="Found keys", description="Keys found near the station")

    with pytest.raises(HTTPException) as error:
        await items_module._save_item(payload, None, "user-1", FoundItem)

    assert error.value.status_code == 400


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
    monkeypatch.setenv("SUPABASE_URL", "https://project.supabase.co")
    image_urls = [
        "https://project.supabase.co/storage/v1/object/sign/report-images/"
        f"{index}.jpg?token=short-lived"
        for index in range(1, 4)
    ]

    async def no_moderation(title, description, **_):
        return None

    async def no_embedding(text):
        return [0.0] * 1536

    no_image_embedding = AsyncMock(return_value=None)

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
        image_url=image_urls[0],
        image_urls=image_urls,
        report_location="मध्यवर्ती स्थानक",
        category="पिशवी",
        source_language="mr",
        community_guidelines_accepted=True,
    )

    record = await items_module._save_item(payload, session, "user-1", FoundItem)

    assert record.title == "निळी पिशवी"
    assert record.description == "स्थानकावर पिशवी हरवली"
    assert record.title_en == "Blue bag"
    assert record.description_en == "A bag lost at the station"
    assert record.report_location_en == "Central station"
    assert record.category_en == "Bag"
    assert record.image_url == (
        "storage://report-images/1.jpg"
    )
    assert record.image_urls == [
        "storage://report-images/1.jpg",
        "storage://report-images/2.jpg",
        "storage://report-images/3.jpg",
    ]
    no_image_embedding.assert_not_awaited()
    session.add.assert_called_once_with(record)


@pytest.mark.asyncio
async def test_report_creation_infers_category_from_english_translation(monkeypatch):
    embedding_inputs = []

    async def no_moderation(title, description, **_):
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
        community_guidelines_accepted=True,
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
    signed_url_batches = []

    def sign_image_urls(urls):
        signed_url_batches.append(urls)
        return list(urls)

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
    monkeypatch.setattr(admin_module, "refresh_private_image_urls", sign_image_urls)
    results = await admin_module.list_all_items(session=session, _="admin-1")

    assert results[0]["title"] == "Blue bag"
    assert results[0]["description"] == "A bag lost at the station"
    assert results[0]["category"] == "Bag"
    assert results[0]["translation_available"] is True
    assert results[0]["image_urls"] == ["https://cdn.example/one.jpg", "https://cdn.example/two.jpg"]
    assert signed_url_batches == [[
        "https://cdn.example/one.jpg",
        "https://cdn.example/one.jpg",
        "https://cdn.example/two.jpg",
    ]]
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

    async def no_moderation(title, description, **_):
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
        community_guidelines_accepted=True,
    )

    updated = await items_module.update_item("lost", "lost-1", payload, session=session, uid="user-1")

    assert updated["title"] == "नवी पिशवी"
    assert updated["source_language"] == "mr"
    assert report.title_en == "New bag"
    assert report.description_en == "New description"
    assert report.report_location_en == "Central station"
    assert report.category_en == "Bag"


@pytest.mark.asyncio
async def test_report_update_can_remove_all_saved_images(monkeypatch):
    report = LostItem(
        id="lost-images",
        created_by="user-1",
        title="Bag",
        description="Blue bag",
        category="other",
        lat=0,
        lng=0,
        image_url="https://cdn.example/one.jpg",
        image_urls=[
            "https://cdn.example/one.jpg",
            "https://cdn.example/two.jpg",
        ],
        edit_count=0,
        created_at=datetime.now(timezone.utc),
    )
    session = Mock(spec=Session)
    session.scalar.return_value = report

    async def no_moderation(*_args, **_kwargs):
        return None

    async def translated_fields(*_args, **_kwargs):
        return None

    monkeypatch.setattr(items_module, "moderate_content", no_moderation)
    monkeypatch.setattr(items_module, "translate_report_fields", translated_fields)
    payload = ItemUpdate(
        title="Bag",
        description="Blue bag",
        category="other",
        community_guidelines_accepted=True,
        image_urls=[],
    )

    updated = await items_module.update_item(
        "lost",
        "lost-images",
        payload,
        session=session,
        uid="user-1",
    )

    assert updated["image_urls"] == []
    assert updated["image_url"] is None


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
async def test_admin_alert_is_omitted_when_linked_reports_are_missing(monkeypatch):
    session = Mock(spec=Session)
    session.get.return_value = None
    monkeypatch.setattr(admin_module, "_supabase_admin_alert_request", Mock(return_value=None))
    alerts = [{
        "found_item_id": "firestore-found-1",
        "lost_item_id": "firestore-lost-1",
        "found_title": "Green wallet",
        "lost_title": "Wallet",
    }]

    translated_alerts = await admin_module._translate_alert_titles(alerts, session)

    assert translated_alerts == []


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
        {"source_id": "found-1", "type": "found", "title": "Green wallet", "description": "Green leather wallet", "image_url": "https://images.test/found.jpg"},
        {"source_id": "lost-1", "type": "lost", "title": "Wallet", "description": "Lost near station", "image_url": "https://images.test/lost.jpg"},
    ]
    monkeypatch.setattr(admin_module, "_supabase_admin_alert_request", Mock(return_value=response))

    translated_alerts = await admin_module._translate_alert_titles(alerts, session)

    assert translated_alerts[0]["found_description"] == "Green leather wallet"
    assert translated_alerts[0]["lost_description"] == "Lost near station"
    assert translated_alerts[0]["found_image_url"] == "https://images.test/found.jpg"
    assert translated_alerts[0]["lost_image_url"] == "https://images.test/lost.jpg"


@pytest.mark.asyncio
async def test_admin_alert_is_preserved_when_image_index_lookup_fails(monkeypatch):
    session = Mock(spec=Session)
    session.get.return_value = None
    alerts = [{
        "found_item_id": "found-1",
        "lost_item_id": "lost-1",
        "found_title": "Green wallet",
        "lost_title": "Wallet",
    }]
    monkeypatch.setattr(
        admin_module,
        "_supabase_admin_alert_request",
        Mock(side_effect=admin_module.HTTPException(503, "Image lookup failed")),
    )

    translated_alerts = await admin_module._translate_alert_titles(alerts, session)

    assert translated_alerts[0]["found_title"] == "Green wallet"
    assert translated_alerts[0]["lost_title"] == "Wallet"
    assert translated_alerts[0]["found_image_url"] == ""


def test_admin_alert_review_persists_decision_and_marks_alert_read(monkeypatch):
    alert = SimpleNamespace(
        id="alert-1",
        review_status="pending",
        is_read=False,
        found_item_id="found-1",
        lost_item_id="lost-1",
        confidence=0.91,
    )
    found_item = SimpleNamespace(id="found-1")
    lost_item = SimpleNamespace(id="lost-1", created_by="owner-1")
    session = Mock(spec=Session)
    session.get.side_effect = [alert, found_item, lost_item]
    session.scalar.return_value = SimpleNamespace(id="notification-1")
    monkeypatch.setattr(admin_module, "_supabase_admin_alert_request", lambda *args, **kwargs: None)
    monkeypatch.setattr(admin_module, "send_verified_match_push", lambda *args, **kwargs: 1)

    result = admin_module.review_match_alert(
        "alert-1",
        admin_module.AlertReviewRequest(
            decision="confirmed",
            proof_verified=True,
            verification_notes="Compared the photo and identifying marks.",
        ),
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


@pytest.mark.parametrize("wildcard", ["*", "all", "ALL"])
def test_admin_uid_parser_rejects_wildcard_configuration(wildcard):
    assert admin_module._parse_admin_uids(wildcard) == set()


@pytest.mark.asyncio
async def test_report_matching_does_not_allow_reading_another_users_report(monkeypatch):
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "admin-user")
    session = Mock(spec=Session)
    session.scalar.return_value = SimpleNamespace(
        created_by="report-owner",
        email_verified=False,
    )

    with pytest.raises(HTTPException) as exc:
        await items_module.match_items(
            MatchRequest(lost_item_id="private-report"),
            session=session,
            uid="different-user",
        )

    assert exc.value.status_code == 403


@pytest.mark.asyncio
async def test_image_matching_no_longer_calls_external_matching_function(monkeypatch):
    session = Mock(spec=Session)
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "admin-user")
    monkeypatch.setattr(items_module, "create_image_embedding", AsyncMock(return_value=None))
    results = await items_module.match_items(
        MatchRequest(imageUrl="https://images.example.test/report.jpg", targetType="lost"),
        session=session,
        uid="admin-user",
    )

    assert results == []
    assert not hasattr(items_module, "_find_cloudinary_image_matches")


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
            query_identifier="Blue wallet",
            matched_items=[LostItem(id="lost-item-1", title="Test phone", created_by="u1", description="Lost phone", category="electronics", lat=0, lng=0)],
            match_type="IMAGE",
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
            query_identifier="Blue wallet",
            matched_items=[{"id": "x"}],
            match_type="IMAGE",
        )
        await scheduled_task

    assert sent["title"].startswith("IMAGE")
    assert sent["score"] == 1.0
    assert "Blue wallet" in sent["details"]


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
        query_identifier="Blue wallet",
        matched_items=[{"id": "x"}],
        match_type="IMAGE",
    )

    assert "coro" in scheduled


@pytest.mark.asyncio
async def test_startup_adds_translation_columns_without_device_identifiers(monkeypatch):
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
        assert "imei" not in columns
        assert {"title_en", "description_en", "report_location_en", "category_en", "source_language"} <= columns
    engine.dispose()


def _make_visual_search_session(query_report, candidates=None):
    session = Mock(spec=Session)
    session.scalar.return_value = query_report
    session.scalars.return_value.all.return_value = candidates or []
    return session


def _visual_candidate(report_id, score, **overrides):
    vector = [score, (1 - score * score) ** 0.5] + [0.0] * 510
    values = {
        "id": report_id,
        "title": f"Report {report_id}",
        "category": "Accessories",
        "report_date": "2026-01-15",
        "image_url": f"storage://report-images/{report_id}.jpg",
        "image_urls": None,
        "image_embedding": vector,
        "image_embedding_model": "ViT-B-32:openai:v1",
        "hidden_from_public": False,
        "status": "FOUND",
        "description": "private report description",
        "report_location": "private exact location",
        "created_by": "another-user",
        "lat": 12.345,
        "lng": 67.89,
    }
    values.update(overrides)
    return SimpleNamespace(**values)


@pytest.mark.asyncio
async def test_visual_search_hides_non_owner_and_inactive_own_reports(monkeypatch):
    monkeypatch.setattr(items_module, "image_embeddings_enabled", lambda: True)
    for query_report in (
        None,
        SimpleNamespace(
            created_by="another-user",
            image_url="storage://report-images/private.jpg",
            image_urls=None,
            image_embedding=[1.0] + [0.0] * 511,
            image_embedding_model="ViT-B-32:openai:v1",
            hidden_from_public=False,
            status="LOST",
        ),
        SimpleNamespace(
            created_by="report-owner",
            image_url="storage://report-images/closed.jpg",
            image_urls=None,
            image_embedding=[1.0] + [0.0] * 511,
            hidden_from_public=False,
            status="RECOVERED",
        ),
        SimpleNamespace(
            created_by="report-owner",
            image_url="storage://report-images/hidden.jpg",
            image_urls=None,
            image_embedding=[1.0] + [0.0] * 511,
            hidden_from_public=True,
            status="LOST",
        ),
    ):
        session = _make_visual_search_session(query_report)
        with pytest.raises(HTTPException) as error:
            await items_module.search_visual_matches(
                "lost", "report-1", session=session, uid="report-owner"
            )
        assert error.value.status_code == 404


@pytest.mark.asyncio
async def test_visual_search_rejects_reports_without_photos(monkeypatch):
    report = SimpleNamespace(
        created_by="report-owner",
        image_url=None,
        image_urls=None,
        image_embedding=None,
        hidden_from_public=False,
        status="LOST",
    )
    with pytest.raises(HTTPException) as error:
        await items_module.search_visual_matches(
            "lost",
            "report-1",
            session=_make_visual_search_session(report),
            uid="report-owner",
        )
    assert error.value.status_code == 422


@pytest.mark.asyncio
async def test_visual_search_requires_embedding_service(monkeypatch):
    report = SimpleNamespace(
        created_by="report-owner",
        image_url="storage://report-images/query.jpg",
        image_urls=None,
        image_embedding=None,
        hidden_from_public=False,
        status="LOST",
    )
    monkeypatch.setattr(items_module, "image_embeddings_enabled", lambda: False)
    with pytest.raises(HTTPException) as error:
        await items_module.search_visual_matches(
            "lost",
            "report-1",
            session=_make_visual_search_session(report),
            uid="report-owner",
        )
    assert error.value.status_code == 503


@pytest.mark.asyncio
async def test_visual_search_threshold_order_limit_and_private_response(monkeypatch):
    query_vector = [1.0] + [0.0] * 511
    report = SimpleNamespace(
        created_by="report-owner",
        image_url=None,
        image_urls=["storage://report-images/query.jpg"],
        image_embedding=query_vector,
        image_embedding_model="ViT-B-32:openai:v1",
        hidden_from_public=False,
        status="LOST",
    )
    candidates = [
        *(_visual_candidate(f"eligible-{index}", score) for index, score in enumerate(
            [0.71, 0.72, 0.73, 0.74, 0.75, 0.76, 0.77, 0.78, 0.79, 0.80, 0.81, 0.82]
        )),
        _visual_candidate("below-threshold", 0.69),
        _visual_candidate("not-indexed", 0.0, image_embedding=None),
        _visual_candidate("hidden", 0.99, hidden_from_public=True),
        _visual_candidate("inactive", 0.99, status="RECOVERED"),
        _visual_candidate("no-photo", 0.99, image_url=None),
    ]
    monkeypatch.setattr(items_module, "image_embeddings_enabled", lambda: True)
    monkeypatch.setattr(
        items_module,
        "refresh_private_image_urls",
        lambda urls: [f"https://signed.example/{index}" for index, _ in enumerate(urls)],
    )

    response = await items_module.search_visual_matches(
        "lost",
        "report-1",
        session=_make_visual_search_session(report, candidates),
        uid="report-owner",
    )
    validated = items_module.VisualSearchResponse.model_validate(response).model_dump()

    assert len(validated["matches"]) == 10
    assert validated["matches"][0]["score"] == pytest.approx(0.82)
    assert validated["matches"][-1]["score"] == pytest.approx(0.73)
    assert validated["searched_reports"] == 13
    assert validated["unindexed_reports"] == 1
    preview = validated["matches"][0]["item"]
    assert preview["report_type"] == "FOUND"
    assert set(preview) == {"title", "category", "report_date", "image_url", "report_type"}


@pytest.mark.asyncio
async def test_visual_search_reindexes_stale_vectors_for_my_reports(monkeypatch):
    model_id = "MobileCLIP-S1:test:v1"
    vector = [1.0] + [0.0] * 511
    query_report = SimpleNamespace(
        created_by="report-owner",
        image_url="storage://report-images/query.jpg",
        image_urls=None,
        image_embedding=vector,
        image_embedding_model=model_id,
        hidden_from_public=False,
        status="LOST",
    )
    candidate = _visual_candidate(
        "stale-candidate",
        0.0,
        image_embedding=None,
        image_embedding_model=None,
    )
    monkeypatch.setattr(items_module, "image_embeddings_enabled", lambda: True)
    monkeypatch.setattr(items_module, "image_embedding_model_id", lambda: model_id)
    monkeypatch.setattr(items_module, "refresh_private_image_urls", lambda urls: ["https://signed.example/photo"])

    async def create_embedding(_):
        return vector

    monkeypatch.setattr(items_module, "create_image_embedding", create_embedding)
    session = _make_visual_search_session(query_report, [candidate])

    response = await items_module.search_visual_matches(
        "lost",
        "report-1",
        session=session,
        uid="report-owner",
    )

    assert candidate.image_embedding == vector
    assert candidate.image_embedding_model == model_id
    assert response["searched_reports"] == 1
    assert response["unindexed_reports"] == 0
    assert len(response["matches"]) == 1