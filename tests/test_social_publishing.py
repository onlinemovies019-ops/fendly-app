import asyncio
import hashlib
import io
import time
from unittest.mock import AsyncMock

import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool
from fastapi import BackgroundTasks
from fastapi import HTTPException
from PIL import Image, ImageDraw

import social_publishing
import social_poster
from models import Base, FoundItem, LostItem, SocialAccount, SocialOAuthState, SocialPublication
from routers import items as items_module


def _session_factory():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    return sessionmaker(bind=engine, expire_on_commit=False)


def test_social_token_encryption_round_trips(monkeypatch):
    monkeypatch.setenv("APP_SECRET_KEY", "test-secret-key-that-is-long-enough")
    encrypted = social_publishing._encrypt_token("private-provider-token")

    assert encrypted != "private-provider-token"
    assert "private-provider-token" not in encrypted
    assert social_publishing._decrypt_token(encrypted) == "private-provider-token"


def test_oauth_state_is_consumed_once():
    factory = _session_factory()
    raw_state = "single-use-state"
    state_digest = hashlib.sha256(raw_state.encode("utf-8")).hexdigest()
    with factory() as session:
        session.add(
            SocialOAuthState(
                state_digest=state_digest,
                provider="meta",
                created_by="admin",
                expires_at=int(time.time()) + 60,
            )
        )
        session.commit()

        social_publishing._consume_state(session, raw_state)

        assert session.get(SocialOAuthState, state_digest) is None


def test_meta_oauth_callback_stores_selected_page_and_linked_instagram(monkeypatch):
    factory = _session_factory()
    monkeypatch.setenv("APP_SECRET_KEY", "test-secret-key-that-is-long-enough")
    monkeypatch.setenv("META_APP_ID", "meta-app-id")
    monkeypatch.setenv("META_APP_SECRET", "meta-app-secret")
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setenv("META_REDIRECT_URI", "https://api.example.test/api/social/callback/meta")
    monkeypatch.setattr(social_publishing, "_consume_state", lambda *_: None)

    class FakeResponse:
        is_error = False

        def __init__(self, value):
            self.value = value

        def json(self):
            return self.value

    def fake_get(url, params, timeout):
        if url.endswith("/oauth/access_token"):
            return FakeResponse({"access_token": "user-token"})
        assert url.endswith("/me/accounts")
        return FakeResponse(
            {
                "data": [
                    {
                        "id": "fendly-page-id",
                        "name": "Fendly Community",
                        "access_token": "page-token",
                        "instagram_business_account": {
                            "id": "fendly-instagram-id",
                            "username": "fendly_community",
                        },
                    }
                ]
            }
        )

    monkeypatch.setattr(social_publishing.httpx, "get", fake_get)

    with factory() as session:
        social_publishing._finish_meta_oauth(session, "auth-code", "state")

        facebook = session.get(SocialAccount, "facebook")
        instagram = session.get(SocialAccount, "instagram")
        assert facebook is not None
        assert facebook.account_id == "fendly-page-id"
        assert facebook.account_name == "Fendly Community"
        assert social_publishing._decrypt_token(facebook.access_token_encrypted) == "page-token"
        assert instagram is not None
        assert instagram.account_id == "fendly-instagram-id"
        assert instagram.account_name == "fendly_community"


def test_meta_oauth_page_id_mismatch_reports_available_pages_without_tokens(monkeypatch):
    factory = _session_factory()
    monkeypatch.setenv("APP_SECRET_KEY", "test-secret-key-that-is-long-enough")
    monkeypatch.setenv("META_APP_ID", "meta-app-id")
    monkeypatch.setenv("META_APP_SECRET", "meta-app-secret")
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setenv("META_REDIRECT_URI", "https://api.example.test/api/social/callback/meta")
    monkeypatch.setenv("META_PAGE_ID", "incorrect-page-id")
    monkeypatch.setattr(social_publishing, "_consume_state", lambda *_: None)

    class FakeResponse:
        is_error = False

        def __init__(self, value):
            self.value = value

        def json(self):
            return self.value

    def fake_get(url, params, timeout):
        if url.endswith("/oauth/access_token"):
            return FakeResponse({"access_token": "user-token"})
        return FakeResponse(
            {
                "data": [
                    {
                        "id": "actual-page-id",
                        "name": "Fendly Community",
                        "access_token": "private-page-token",
                    }
                ]
            }
        )

    monkeypatch.setattr(social_publishing.httpx, "get", fake_get)

    with factory() as session, pytest.raises(RuntimeError) as error:
        social_publishing._finish_meta_oauth(session, "auth-code", "state")

    assert "META_PAGE_ID did not match an available Page" in str(error.value)
    assert "actual-page-id (Fendly Community)" in str(error.value)
    assert "private-page-token" not in str(error.value)


def test_social_caption_excludes_report_details_and_masks_contact_values(monkeypatch):
    app_url = "https://download.fendly.example"
    monkeypatch.setenv("FENDLY_APP_URL", app_url)
    report = LostItem(
        id="7e5e744d-1be9-4c47-b1d1-95a9d817d980",
        created_by="report-owner",
        title="Blue bag call +1 555 123 4567",
        description="Contains a home address and a private phone number.",
        category="bag",
        lat=12.3,
        lng=45.6,
        report_location="Private address",
    )

    caption = social_publishing._safe_caption(report, "lost")

    assert "Blue bag" in caption
    assert "555 123 4567" not in caption
    assert "home address" not in caption
    assert "Private address" not in caption
    assert "12.3" not in caption
    assert caption.endswith(
        f"Fendly: {app_url}/item/7e5e744d-1be9-4c47-b1d1-95a9d817d980"
    )
    assert "Get the Fendly app" not in caption


def test_generated_community_poster_uses_the_report_link(monkeypatch):
    monkeypatch.setenv("FENDLY_APP_URL", "https://download.fendly.example")
    report = LostItem(
        id="7e5e744d-1be9-4c47-b1d1-95a9d817d980",
        created_by="report-owner",
        title="White cat",
        description="Private report details",
        category="animal",
        lat=0,
        lng=0,
        report_location="Private address",
        report_date="Private date",
    )
    poster_args = {}
    original_renderer = social_poster.render_community_poster

    def render_poster(title, report_type, category, report_url, photo, focus_box=None):
        poster_args["report_url"] = report_url
        return original_renderer(
            title, report_type, category, report_url, photo, focus_box
        )

    monkeypatch.setattr(social_poster, "render_community_poster", render_poster)
    poster_bytes = asyncio.run(social_poster.render_report_poster(report, "lost"))
    with Image.open(io.BytesIO(poster_bytes)) as poster:
        assert poster.format == "JPEG"
        assert poster.size == (1080, 1350)
    assert poster_args["report_url"] == (
        "https://download.fendly.example/item/7e5e744d-1be9-4c47-b1d1-95a9d817d980"
    )


def test_social_poster_focuses_animal_and_person_subjects_near_top():
    assert social_poster._poster_photo_centering("Parrot", "Animals") == (0.5, 0.08)
    assert social_poster._poster_photo_centering("Missing child", "People") == (0.5, 0.08)
    assert social_poster._poster_photo_centering("Blue backpack", "Items") == (0.5, 0.5)


def test_social_poster_crop_keeps_top_subject_in_image():
    photo = Image.new("RGB", (100, 200), "blue")
    ImageDraw.Draw(photo).rectangle((0, 0, 99, 24), fill="red")

    cropped = social_poster._rounded_photo(photo, (100, 50), (0.5, 0.08))

    assert cropped.getpixel((50, 5))[0] > cropped.getpixel((50, 5))[2]


def test_gemini_animal_face_detection_returns_scaled_face_box(monkeypatch):
    monkeypatch.setenv("GEMINI_API_KEY", "test-gemini-key")
    request_data = {}

    class FakeResponse:
        def raise_for_status(self):
            return None

        def json(self):
            return {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": (
                                '{"found":true,"subject":"animal","x_min":100,'
                                '"y_min":100,"x_max":500,"y_max":500}'
                            )
                        }]
                    }
                }]
            }

    class FakeClient:
        def __init__(self, timeout):
            request_data["timeout"] = timeout

        async def __aenter__(self):
            return self

        async def __aexit__(self, *_args):
            return None

        async def post(self, url, json, headers):
            request_data.update(url=url, json=json, headers=headers)
            return FakeResponse()

    monkeypatch.setattr(social_poster.httpx, "AsyncClient", FakeClient)
    photo = Image.new("RGB", (200, 400), "green")

    focus_box = asyncio.run(
        social_poster._detect_subject_face(photo, "Parrot", "Animals")
    )

    assert focus_box == (20, 40, 100, 200)
    assert request_data["headers"] == {"x-goog-api-key": "test-gemini-key"}
    assert request_data["json"]["generationConfig"]["responseMimeType"] == "application/json"
    assert "inline_data" in request_data["json"]["contents"][0]["parts"][1]


def test_subject_face_box_rejects_wrong_subject_or_invalid_coordinates():
    payload = {
        "candidates": [{
            "content": {
                "parts": [{
                    "text": (
                        '{"found":true,"subject":"human","x_min":100,'
                        '"y_min":100,"x_max":500,"y_max":500}'
                    )
                }]
            }
        }]
    }

    assert social_poster._parse_subject_face_box(payload, 200, 400, "animal") is None
    assert social_poster._parse_subject_face_box({}, 200, 400, "animal") is None


def test_social_poster_crop_centers_detected_animal_face():
    photo = Image.new("RGB", (200, 400), "blue")
    ImageDraw.Draw(photo).rectangle((80, 270, 120, 320), fill="red")

    cropped = social_poster._rounded_photo(
        photo,
        (200, 100),
        (0.5, 0.08),
        (80, 270, 120, 320),
    )

    center_pixel = cropped.getpixel((100, 55))
    assert center_pixel[0] > center_pixel[2]


def test_schedule_report_publications_only_queues_connected_channels():
    factory = _session_factory()
    with factory() as session:
        session.add_all(
            [
                SocialAccount(
                    provider=provider,
                    account_id=f"{provider}-id",
                    account_name=provider,
                    access_token_encrypted="encrypted-token",
                )
                for provider in ("facebook", "instagram")
            ]
        )
        report = FoundItem(
            id="found-social-report",
            created_by="report-owner",
            title="Blue bag",
            description="Details stay private.",
            category="bag",
            lat=0,
            lng=0,
            social_share_consent=True,
            image_url=None,
            social_poster_url="https://storage.example.test/community.jpg",
        )
        session.add(report)
        session.flush()
        tasks = BackgroundTasks()

        social_publishing.schedule_report_publications(session, report, "found", tasks)

        jobs = session.scalars(
            select(SocialPublication).order_by(SocialPublication.provider)
        ).all()
        assert [(job.provider, job.status) for job in jobs] == [
            ("facebook", "pending"),
            ("instagram", "pending"),
        ]
        assert len(tasks.tasks) == 2

        report.social_share_consent = False
        social_publishing.schedule_report_publications(session, report, "found", None)
        assert len(session.scalars(select(SocialPublication)).all()) == 2


def test_social_publication_scheduling_ignores_legacy_x_account():
    factory = _session_factory()
    with factory() as session:
        session.add(
            SocialAccount(
                provider="x",
                account_id="legacy-x-account",
                account_name="Legacy X",
                access_token_encrypted="legacy-token",
            )
        )
        report = FoundItem(
            id="report-with-only-legacy-x-connection",
            created_by="report-owner",
            title="Blue bag",
            description="Details stay private.",
            category="bag",
            lat=0,
            lng=0,
            social_share_consent=True,
        )
        session.add(report)
        session.flush()

        with pytest.raises(HTTPException) as error:
            social_publishing.schedule_report_publications(session, report, "found", None)

        assert error.value.status_code == 503
        assert session.scalars(select(SocialPublication)).all() == []


def test_social_status_only_exposes_facebook_and_instagram_accounts():
    factory = _session_factory()
    with factory() as session:
        session.add_all(
            [
                SocialAccount(
                    provider=provider,
                    account_id=f"{provider}-id",
                    account_name=provider,
                    access_token_encrypted="encrypted-token",
                )
                for provider in ("facebook", "x")
            ]
        )
        session.commit()

        status = social_publishing.get_social_status(session, "admin")

    assert set(status) == {"facebook", "instagram"}
    assert status["facebook"]["connected"] is True
    assert status["instagram"]["connected"] is False


def test_social_api_only_registers_meta_connection_and_callback_routes():
    route_paths = {route.path for route in social_publishing.router.routes}

    assert "/api/social/connect/meta" in route_paths
    assert "/api/social/callback/meta" in route_paths
    assert "/api/social/connect/x" not in route_paths
    assert "/api/social/callback/x" not in route_paths


def test_social_sharing_opt_in_requires_a_connected_brand_account():
    factory = _session_factory()
    with factory() as session:
        report = FoundItem(
            id="report-without-social-setup",
            created_by="report-owner",
            title="Blue bag",
            description="Details stay private.",
            category="bag",
            lat=0,
            lng=0,
            social_share_consent=True,
        )
        session.add(report)
        session.flush()

        try:
            social_publishing.schedule_report_publications(session, report, "found", None)
        except HTTPException as error:
            assert error.status_code == 503
            assert "Uncheck social sharing" in error.detail
        else:
            raise AssertionError("Social sharing should require a connected brand account")


def test_report_submission_persists_consent_and_creates_publication_job(monkeypatch):
    factory = _session_factory()
    with factory() as session:
        session.add(
            SocialAccount(
                provider="facebook",
                account_id="fendly-page",
                account_name="Fendly",
                access_token_encrypted="encrypted-token",
            )
        )
        session.commit()
        monkeypatch.setattr(items_module, "moderate_content", AsyncMock(return_value=None))
        monkeypatch.setattr(items_module, "create_embedding", AsyncMock(return_value=None))
        monkeypatch.setattr(items_module, "create_image_embedding", AsyncMock(return_value=None))
        monkeypatch.setattr(items_module, "translate_report_fields", AsyncMock(return_value=None))
        monkeypatch.setattr(items_module, "_process_created_report", AsyncMock(return_value=None))
        monkeypatch.setattr(items_module, "render_report_poster", AsyncMock(return_value=b"poster"))
        monkeypatch.setattr(
            items_module,
            "_store_image",
            AsyncMock(return_value="https://storage.example.test/community-poster.jpg"),
        )
        tasks = BackgroundTasks()

        report = asyncio.run(
            items_module.create_item_compat(
                items_module.ItemSubmission(
                    title="Blue bag",
                    description="Black shoulder bag",
                    type="found",
                    social_share_consent=True,
                    community_guidelines_accepted=True,
                ),
                tasks,
                session,
                "report-owner",
            )
        )

        assert report.social_share_consent is True
        assert report.social_poster_url == "https://storage.example.test/community-poster.jpg"
        job = session.scalar(select(SocialPublication).where(SocialPublication.report_id == report.id))
        assert job is not None
        assert job.provider == "facebook"
        assert job.status == "pending"
        assert len(tasks.tasks) == 1


def test_facebook_publishes_generated_poster_instead_of_report_photo(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    account = SocialAccount(
        provider="facebook",
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )
    report = LostItem(
        created_by="report-owner",
        title="White cat",
        description="Private details",
        category="animal",
        lat=0,
        lng=0,
        image_url="https://storage.example.test/original.jpg",
        social_poster_url="https://storage.example.test/poster.jpg",
    )
    requested = {}

    class FakeResponse:
        is_error = False

        @staticmethod
        def json():
            return {"post_id": "facebook-post-id"}

    def fake_post(url, data, timeout):
        requested.update(url=url, data=data)
        return FakeResponse()

    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")
    monkeypatch.setattr(social_publishing.httpx, "post", fake_post)

    post_id = social_publishing._publish_facebook(account, report, "caption")

    assert post_id == "facebook-post-id"
    assert requested["data"]["url"] == "https://storage.example.test/poster.jpg"
    assert requested["data"]["url"] != report.image_url


def test_instagram_publishes_generated_poster(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    account = SocialAccount(
        provider="instagram",
        account_id="instagram-id",
        account_name="fendly",
        access_token_encrypted="encrypted-token",
    )
    report = FoundItem(
        created_by="report-owner",
        title="White cat",
        description="Private details",
        category="animal",
        lat=0,
        lng=0,
        image_url="https://storage.example.test/original.jpg",
        social_poster_url="https://storage.example.test/poster.jpg",
    )
    requests = []

    class FakeResponse:
        is_error = False

        def __init__(self, value):
            self.value = value

        def json(self):
            return self.value

    def fake_post(url, data, timeout):
        requests.append((url, data))
        if url.endswith("/media"):
            return FakeResponse({"id": "creation-id"})
        return FakeResponse({"id": "instagram-post-id"})

    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")
    monkeypatch.setattr(social_publishing.httpx, "post", fake_post)

    post_id = social_publishing._publish_instagram(account, report, "caption")

    assert post_id == "instagram-post-id"
    assert requests[0][1]["image_url"] == "https://storage.example.test/poster.jpg"
    assert requests[0][1]["image_url"] != report.image_url


def test_publication_worker_records_success(monkeypatch):
    factory = _session_factory()
    monkeypatch.setattr(social_publishing, "SessionLocal", factory)
    monkeypatch.setenv("APP_SECRET_KEY", "test-secret-key-that-is-long-enough")

    with factory() as session:
        report = LostItem(
            id="lost-social-report",
            created_by="report-owner",
            title="Blue bag",
            description="Private detail",
            category="bag",
            lat=0,
            lng=0,
            social_share_consent=True,
        )
        account = SocialAccount(
            provider="facebook",
            account_id="page-id",
            account_name="Fendly",
            access_token_encrypted=social_publishing._encrypt_token("private-token"),
        )
        publication = SocialPublication(
            id="publication-1",
            report_id=report.id,
            report_type="lost",
            provider="facebook",
            status="pending",
            next_attempt_at=0,
        )
        session.add_all([report, account, publication])
        session.commit()

    monkeypatch.setattr(social_publishing, "_publish_facebook", lambda *_: "external-post-1")
    social_publishing.process_due_publications()

    with factory() as session:
        saved = session.get(SocialPublication, "publication-1")
        assert saved.status == "published"
        assert saved.external_post_id == "external-post-1"
        assert saved.attempt_count == 1


def test_external_post_feed_refresh_uses_provider_edges_and_pagination(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")

    class FakeResponse:
        status_code = 200
        is_error = False

        def __init__(self, payload):
            self.payload = payload

        def json(self):
            return self.payload

    calls = []

    def fake_get(url, params, timeout):
        calls.append((url, params, timeout))
        if url.endswith("/page-id/published_posts"):
            if "after" not in params:
                return FakeResponse(
                    {
                        "data": [{"id": "facebook-post"}],
                        "paging": {
                            "next": "https://graph.facebook.com/next",
                            "cursors": {"after": "page-cursor"},
                        },
                    }
                )
            assert params["after"] == "page-cursor"
            return FakeResponse({"data": [{"id": "older-facebook-post"}]})
        assert url.endswith("/instagram-id/media")
        return FakeResponse({"data": [{"id": "instagram-post"}]})

    monkeypatch.setattr(social_publishing.httpx, "get", fake_get)
    facebook = SocialAccount(
        provider="facebook",
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )
    instagram = SocialAccount(
        provider="instagram",
        account_id="instagram-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )

    assert social_publishing._list_external_post_ids(
        facebook,
        {"facebook-post", "older-facebook-post"},
    ) == (
        {"facebook-post", "older-facebook-post"},
        True,
    )
    assert social_publishing._list_external_post_ids(instagram, {"instagram-post"}) == (
        {"instagram-post"},
        True,
    )
    assert calls[0][0] == "https://graph.facebook.com/v23.0/page-id/published_posts"
    assert calls[0][1] == {"fields": "id", "limit": 100, "access_token": "page-token"}
    assert calls[2][0] == "https://graph.facebook.com/v23.0/instagram-id/media"


def test_external_post_lookup_reports_missing_posts_and_inconclusive_errors(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")

    class FakeResponse:
        def __init__(self, status_code, payload):
            self.status_code = status_code
            self.is_error = status_code >= 400
            self.payload = payload

        def json(self):
            return self.payload

    responses = iter([
        FakeResponse(400, {"error": {"code": 100, "error_subcode": 33}}),
        FakeResponse(400, {"error": {"code": 190}}),
    ])
    monkeypatch.setattr(
        social_publishing.httpx,
        "get",
        lambda *_args, **_kwargs: next(responses),
    )
    account = SocialAccount(
        provider="facebook",
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )

    assert social_publishing._check_external_post(account, "deleted-post") == "unavailable"
    assert social_publishing._check_external_post(account, "inaccessible-post") == "check_failed"


def test_publication_refresh_reconciles_feed_and_preserves_inconclusive_checks(monkeypatch):
    factory = _session_factory()
    monkeypatch.setattr(
        social_publishing,
        "_list_external_post_ids",
        lambda account, _expected_post_ids: (
            (set(), False) if account.provider == "facebook" else (set(), True)
        ),
    )
    monkeypatch.setattr(
        social_publishing,
        "_check_external_post",
        lambda _account, post_id: {
            "live-facebook-post": "available",
            "temporarily-unavailable-post": "check_failed",
            "deleted-facebook-post": "unavailable",
        }[post_id],
    )
    with factory() as session:
        accounts = [
            SocialAccount(
                provider=provider,
                account_id=f"{provider}-id",
                account_name="Fendly",
                access_token_encrypted="encrypted-token",
            )
            for provider in ("instagram", "facebook")
        ]
        publications = [
            SocialPublication(
                id="publication-refresh",
                report_id="report-1",
                report_type="lost",
                provider="instagram",
                status="published",
                external_post_id="deleted-instagram-post",
            ),
            SocialPublication(
                id="publication-live",
                report_id="report-live",
                report_type="found",
                provider="facebook",
                status="published",
                external_post_id="live-facebook-post",
            ),
            SocialPublication(
                id="publication-check-failed",
                report_id="report-3",
                report_type="lost",
                provider="facebook",
                status="published",
                external_post_id="temporarily-unavailable-post",
            ),
            SocialPublication(
                id="publication-fallback-deleted",
                report_id="report-deleted",
                report_type="lost",
                provider="facebook",
                status="published",
                external_post_id="deleted-facebook-post",
            ),
        ]
        legacy_x_publication = SocialPublication(
            id="legacy-x-publication",
            report_id="report-2",
            report_type="lost",
            provider="x",
            status="failed",
            external_post_id="legacy-x-post",
        )
        session.add_all([*accounts, *publications, legacy_x_publication])
        session.commit()

        result = social_publishing.refresh_social_publications(
            limit=20,
            session=session,
            _="admin-uid",
        )
        assert session.get(SocialPublication, "publication-refresh").status == "removed"
        assert session.get(SocialPublication, "publication-live").status == "published"
        assert session.get(SocialPublication, "publication-check-failed").status == "published"
        assert session.get(SocialPublication, "publication-fallback-deleted").status == "removed"
        visible = social_publishing.list_social_publications(
            limit=20,
            session=session,
            _="admin-uid",
        )
        assert {item["report_id"] for item in visible} == {"report-live", "report-3"}

    results = {item["report_id"]: item for item in result}
    assert set(results) == {"report-live", "report-3"}
    assert results["report-live"]["platform_status"] == "available"
    assert results["report-3"]["platform_status"] == "check_failed"
    assert results["report-3"]["status"] == "published"


def test_incomplete_external_post_feed_is_not_treated_as_deleted(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")

    class FakeResponse:
        status_code = 200
        is_error = False

        def json(self):
            return {
                "data": [{"id": "first-page-post"}],
                "paging": {"next": "https://graph.facebook.com/next"},
            }

    monkeypatch.setattr(social_publishing.httpx, "get", lambda *_args, **_kwargs: FakeResponse())
    account = SocialAccount(
        provider="facebook",
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )

    post_ids, complete = social_publishing._list_external_post_ids(
        account,
        {"post-missing-from-feed"},
    )

    assert post_ids == {"first-page-post"}
    assert not complete


def test_publication_list_excludes_legacy_x_and_removed_jobs():
    factory = _session_factory()
    with factory() as session:
        session.add_all([
            SocialPublication(
                id="facebook-publication",
                report_id="report-facebook",
                report_type="lost",
                provider="facebook",
                status="failed",
            ),
            SocialPublication(
                id="legacy-x-publication",
                report_id="report-x",
                report_type="lost",
                provider="x",
                status="failed",
            ),
            SocialPublication(
                id="removed-facebook-publication",
                report_id="report-removed",
                report_type="lost",
                provider="facebook",
                status="removed",
            ),
        ])
        session.commit()

        result = social_publishing.list_social_publications(
            limit=20,
            session=session,
            _="admin-uid",
        )

    assert [item["provider"] for item in result] == ["facebook"]


def test_publication_worker_does_not_retry_ambiguous_provider_failure(monkeypatch):
    factory = _session_factory()
    monkeypatch.setattr(social_publishing, "SessionLocal", factory)
    monkeypatch.setenv("APP_SECRET_KEY", "test-secret-key-that-is-long-enough")
    calls = []

    with factory() as session:
        report = LostItem(
            id="lost-social-report-failure",
            created_by="report-owner",
            title="Blue bag",
            description="Private detail",
            category="bag",
            lat=0,
            lng=0,
            social_share_consent=True,
        )
        account = SocialAccount(
            provider="facebook",
            account_id="page-id",
            account_name="Fendly",
            access_token_encrypted=social_publishing._encrypt_token("private-token"),
        )
        publication = SocialPublication(
            id="publication-failure",
            report_id=report.id,
            report_type="lost",
            provider="facebook",
            status="pending",
            next_attempt_at=0,
        )
        session.add_all([report, account, publication])
        session.commit()

    def fail_publication(*_):
        calls.append("attempted")
        raise RuntimeError("Provider response was lost")

    monkeypatch.setattr(social_publishing, "_publish_facebook", fail_publication)
    social_publishing.process_due_publications()
    social_publishing.process_due_publications()

    with factory() as session:
        saved = session.get(SocialPublication, "publication-failure")
        assert saved is not None
        assert saved.status == "failed"
        assert saved.attempt_count == 1
        assert saved.next_attempt_at == 0
        assert saved.last_error is not None
        assert "Not retried automatically" in saved.last_error
        assert calls == ["attempted"]
