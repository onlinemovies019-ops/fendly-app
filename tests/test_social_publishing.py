import asyncio
import hashlib
import io
import time
from unittest.mock import AsyncMock
from urllib.parse import parse_qs, urlsplit

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
            token = (
                "long-lived-user-token"
                if params.get("grant_type") == "fb_exchange_token"
                else "user-token"
            )
            return FakeResponse({"access_token": token})
        if url.endswith("/debug_token"):
            return FakeResponse(
                {
                    "data": {
                        "scopes": [
                            "pages_manage_posts",
                            "instagram_manage_contents",
                        ]
                    }
                }
            )
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
        assert instagram.deletion_access_token_encrypted is not None
        assert social_publishing._decrypt_token(
            instagram.deletion_access_token_encrypted
        ) == "long-lived-user-token"


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
            token = (
                "long-lived-user-token"
                if params.get("grant_type") == "fb_exchange_token"
                else "user-token"
            )
            return FakeResponse({"access_token": token})
        if url.endswith("/debug_token"):
            return FakeResponse(
                {"data": {"scopes": ["pages_manage_posts"]}}
            )
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

    def render_poster(title, report_type, category, report_url, photo):
        poster_args["report_url"] = report_url
        return original_renderer(
            title, report_type, category, report_url, photo
        )

    monkeypatch.setattr(social_poster, "render_community_poster", render_poster)
    poster_bytes = asyncio.run(social_poster.render_report_poster(report, "lost"))
    with Image.open(io.BytesIO(poster_bytes)) as poster:
        assert poster.format == "JPEG"
        assert poster.size == (1080, 1350)
    assert poster_args["report_url"] == (
        "https://download.fendly.example/item/7e5e744d-1be9-4c47-b1d1-95a9d817d980"
    )


def test_social_poster_uses_person_in_home_message():
    assert social_poster._home_subject("Missing John", "People") == "Person"
    assert social_poster._home_subject("Missing person", "other") == "Person"


def test_social_poster_fits_entire_photo_in_square_frame():
    photo = Image.new("RGB", (200, 100), "green")
    ImageDraw.Draw(photo).rectangle((0, 0, 49, 99), fill="red")
    ImageDraw.Draw(photo).rectangle((150, 0, 199, 99), fill="blue")

    fitted = social_poster._rounded_photo(photo, (100, 100))

    assert fitted.size == (100, 100)
    assert fitted.getpixel((10, 50))[0] > fitted.getpixel((10, 50))[2]
    assert fitted.getpixel((90, 50))[2] > fitted.getpixel((90, 50))[0]
    assert fitted.getpixel((50, 10))[:3] == (231, 229, 218)


def test_social_poster_does_not_call_gemini(monkeypatch):
    monkeypatch.setenv("GEMINI_API_KEY", "test-gemini-key")
    report = LostItem(
        id="report-with-parrot",
        created_by="report-owner",
        title="Parrot",
        description="Green parrot",
        category="animal",
        lat=0,
        lng=0,
        image_url="https://fendly-api.onrender.com/static/uploads/parrot.jpg",
    )
    renderer_args = {}

    def reject_external_request(*_args, **_kwargs):
        raise AssertionError("Social poster generation must not call Gemini")

    def capture_renderer(title, report_type, category, report_url, photo):
        renderer_args["photo"] = photo
        return b"poster"

    monkeypatch.setattr(social_poster.httpx, "AsyncClient", reject_external_request)
    async def load_photo(_url):
        return Image.new("RGB", (200, 400), "green")

    monkeypatch.setattr(social_poster, "_load_report_photo", load_photo)
    monkeypatch.setattr(social_poster, "render_community_poster", capture_renderer)

    poster_bytes = asyncio.run(social_poster.render_report_poster(report, "lost"))

    assert poster_bytes == b"poster"
    assert renderer_args["photo"] is not None


def test_social_poster_still_renders_when_report_photo_cannot_be_loaded(monkeypatch):
    report = LostItem(
        id="report-with-unavailable-photo",
        created_by="report-owner",
        title="Parrot",
        description="Green parrot",
        category="animal",
        lat=0,
        lng=0,
        image_url="https://fendly-api.onrender.com/static/uploads/missing.jpg",
    )
    renderer_args = {}

    async def unavailable_photo(_image_url):
        raise RuntimeError("photo download failed")

    def render_poster(title, report_type, category, report_url, photo):
        renderer_args["photo"] = photo
        return b"poster"

    monkeypatch.setattr(social_poster, "_load_report_photo", unavailable_photo)
    monkeypatch.setattr(social_poster, "render_community_poster", render_poster)

    poster_bytes = asyncio.run(social_poster.render_report_poster(report, "lost"))

    assert poster_bytes == b"poster"
    assert renderer_args["photo"] is None


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
        poster_renderer = AsyncMock(return_value=b"poster")
        monkeypatch.setattr(social_publishing, "render_report_poster", poster_renderer)
        monkeypatch.setattr(
            social_publishing,
            "store_image",
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
        assert report.social_poster_url is None
        job = session.scalar(select(SocialPublication).where(SocialPublication.report_id == report.id))
        assert job is not None
        assert job.provider == "facebook"
        assert job.status == "pending"
        assert len(tasks.tasks) == 2
        assert tasks.tasks[1].func is items_module._process_created_report_background
        poster_renderer.assert_not_awaited()


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
            return {"id": "facebook-photo-id", "post_id": "facebook-post-id"}

    def fake_post(url, data, timeout):
        requested.update(url=url, data=data)
        return FakeResponse()

    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")
    monkeypatch.setattr(social_publishing.httpx, "post", fake_post)

    post_id = social_publishing._publish_facebook(account, report, "caption")

    assert post_id == "facebook-post-id"
    assert requested["data"]["url"] == "https://storage.example.test/poster.jpg"
    assert requested["data"]["url"] != report.image_url


def test_facebook_publication_does_not_treat_photo_id_as_page_post_id(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")

    class FakeResponse:
        is_error = False

        @staticmethod
        def json():
            return {"id": "facebook-photo-id"}

    monkeypatch.setattr(social_publishing.httpx, "post", lambda *_args, **_kwargs: FakeResponse())
    report = LostItem(
        created_by="report-owner",
        title="White cat",
        description="Private details",
        category="animal",
        lat=0,
        lng=0,
        social_poster_url="https://storage.example.test/poster.jpg",
    )
    account = SocialAccount(
        provider="facebook",
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )

    with pytest.raises(RuntimeError, match="photo ID cannot be used"):
        social_publishing._publish_facebook(account, report, "caption")


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


def test_meta_http_error_includes_operation_and_provider_diagnostics():
    class FakeErrorResponse:
        is_error = True
        status_code = 400

        @staticmethod
        def json():
            return {
                "error": {
                    "message": "Invalid image URL",
                    "type": "OAuthException",
                    "code": 9004,
                    "error_subcode": 2207052,
                    "fbtrace_id": "trace-123",
                }
            }

    with pytest.raises(RuntimeError) as raised:
        social_publishing._provider_response(
            FakeErrorResponse(),
            "Instagram media container creation",
        )

    message = str(raised.value)
    assert "Instagram media container creation" in message
    assert "HTTP 400" in message
    assert "code 9004" in message
    assert "subcode 2207052" in message
    assert "Invalid image URL" in message
    assert "trace trace-123" in message


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
            status="failed",
            last_error=social_publishing.UNEXPECTED_PUBLISHING_FAILURE,
            next_attempt_at=0,
        )
        session.add_all([report, account, publication])
        session.commit()

    monkeypatch.setattr(
        social_publishing,
        "render_report_poster",
        AsyncMock(return_value=b"poster-bytes"),
    )
    monkeypatch.setattr(
        social_publishing,
        "store_image",
        AsyncMock(return_value="https://storage.example.test/generated-poster.jpg"),
    )
    published_poster_urls = []

    def publish_facebook(_account, report, _caption):
        published_poster_urls.append(report.social_poster_url)
        return "external-post-1"

    monkeypatch.setattr(social_publishing, "_publish_facebook", publish_facebook)
    social_publishing.process_due_publications()

    with factory() as session:
        saved = session.get(SocialPublication, "publication-1")
        report = session.get(LostItem, "lost-social-report")
        assert saved.status == "published"
        assert saved.external_post_id == "external-post-1"
        assert saved.attempt_count == 1
        assert report.social_poster_url == "https://storage.example.test/generated-poster.jpg"
    assert published_poster_urls == ["https://storage.example.test/generated-poster.jpg"]


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


def test_instagram_post_lookup_uses_user_token_to_confirm_manual_deletion(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    decrypted_tokens = {
        "encrypted-page-token": "page-token",
        "encrypted-user-token": "facebook-user-token",
    }
    monkeypatch.setattr(
        social_publishing,
        "_decrypt_token",
        lambda encrypted: decrypted_tokens[encrypted],
    )
    requested = {}

    class FakeResponse:
        status_code = 400
        is_error = True

        @staticmethod
        def json():
            return {
                "error": {
                    "code": 100,
                    "error_subcode": 33,
                    "message": "Unsupported get request",
                }
            }

    def fake_get(url, params, timeout):
        requested.update(url=url, params=params, timeout=timeout)
        return FakeResponse()

    monkeypatch.setattr(social_publishing.httpx, "get", fake_get)
    account = SocialAccount(
        provider="instagram",
        account_id="instagram-id",
        account_name="Fendly Instagram",
        access_token_encrypted="encrypted-page-token",
        deletion_access_token_encrypted="encrypted-user-token",
    )

    assert social_publishing._check_external_post(account, "deleted-instagram-post") == "unavailable"
    assert requested["params"]["access_token"] == "facebook-user-token"


def test_removing_report_accepts_instagram_post_already_deleted_on_meta(monkeypatch):
    factory = _session_factory()
    monkeypatch.setattr(
        social_publishing,
        "_check_external_post",
        lambda _account, _post_id: "unavailable",
    )

    def unexpected_delete(*_args, **_kwargs):
        pytest.fail("Should not call Meta DELETE for an already unavailable post")

    monkeypatch.setattr(social_publishing, "_delete_meta_post", unexpected_delete)
    with factory() as session:
        publication = SocialPublication(
            id="manually-deleted-instagram-publication",
            report_id="manually-deleted-report",
            report_type="lost",
            provider="instagram",
            status="published",
            external_post_id="deleted-instagram-post",
        )
        session.add(
            SocialAccount(
                provider="instagram",
                account_id="instagram-id",
                account_name="Fendly Instagram",
                access_token_encrypted="encrypted-page-token",
                deletion_access_token_encrypted="encrypted-user-token",
            )
        )
        session.add(publication)
        session.commit()

        failures = social_publishing.remove_report_publications(
            session,
            "manually-deleted-report",
            "lost",
        )

        assert failures == []
        assert session.get(SocialPublication, "manually-deleted-instagram-publication") is None


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
        report = LostItem(
            id="report-facebook",
            created_by="report-owner",
            title="Blue bag",
            description="Private detail",
            category="bag",
            lat=0,
            lng=0,
            social_poster_url="https://storage.example.test/poster.jpg",
            image_url="https://storage.example.test/report-photo.jpg",
            title_en="Blue bag",
            category_en="Apparels and accessories",
        )
        session.add_all([
            report,
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
    assert result[0]["poster_url"] == "https://storage.example.test/poster.jpg"
    assert result[0]["report_image_url"] == "https://storage.example.test/report-photo.jpg"
    assert result[0]["report_title"] == "Blue bag"
    assert result[0]["report_category"] == "Apparels and accessories"


def test_published_image_url_uses_meta_post_media_when_poster_is_missing(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")
    requested = {}

    class FakeResponse:
        is_error = False

        @staticmethod
        def json():
            return {"media_url": "https://storage.example.test/meta-published.jpg"}

    def fake_get(url, params, timeout):
        requested.update(url=url, params=params, timeout=timeout)
        return FakeResponse()

    monkeypatch.setattr(social_publishing.httpx, "get", fake_get)
    account = SocialAccount(
        provider="instagram",
        account_id="instagram-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )
    publication = SocialPublication(
        id="instagram-publication",
        report_id="report-1",
        report_type="lost",
        provider="instagram",
        status="published",
        external_post_id="instagram-post-id",
    )

    image_url = social_publishing._published_post_image_url(account, publication)

    assert image_url == "https://storage.example.test/meta-published.jpg"
    assert requested["url"] == "https://graph.facebook.com/v23.0/instagram-post-id"
    assert requested["params"]["fields"] == "media_url,thumbnail_url"


def test_admin_report_removal_deletes_meta_posts_and_retains_failed_deletions(monkeypatch):
    factory = _session_factory()
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")
    monkeypatch.setattr(
        social_publishing,
        "_check_external_post",
        lambda *_args: "available",
    )
    requests = []

    class FakeResponse:
        def __init__(self, payload, is_error=False, status_code=None):
            self.payload = payload
            self.is_error = is_error
            self.status_code = status_code or (403 if is_error else 200)

        def json(self):
            return self.payload

    def fake_delete(url, params, timeout):
        requests.append((url, params, timeout))
        if url.endswith("already-removed-post"):
            return FakeResponse(
                {"error": {"message": "Post not found", "code": 100}},
                is_error=True,
                status_code=404,
            )
        if url.endswith("instagram-post"):
            return FakeResponse(
                {"error": {"message": "Permission denied", "code": 200}},
                is_error=True,
            )
        return FakeResponse({"success": True})

    monkeypatch.setattr(social_publishing.httpx, "delete", fake_delete)
    with factory() as session:
        session.add_all([
            SocialAccount(
                provider="facebook",
                account_id="page-id",
                account_name="Fendly",
                access_token_encrypted="encrypted-token",
            ),
            SocialAccount(
                provider="instagram",
                account_id="instagram-id",
                account_name="Fendly",
                access_token_encrypted="encrypted-token",
                deletion_access_token_encrypted="encrypted-user-token",
            ),
            SocialPublication(
                id="facebook-publication",
                report_id="fake-report",
                report_type="lost",
                provider="facebook",
                status="published",
                external_post_id="facebook-post",
            ),
            SocialPublication(
                id="instagram-publication",
                report_id="fake-report",
                report_type="lost",
                provider="instagram",
                status="published",
                external_post_id="instagram-post",
            ),
            SocialPublication(
                id="already-removed-publication",
                report_id="already-removed-report",
                report_type="lost",
                provider="facebook",
                status="published",
                external_post_id="already-removed-post",
            ),
        ])
        session.commit()

        failures = social_publishing.remove_report_publications(
            session, "fake-report", "lost"
        )
        already_removed_failures = social_publishing.remove_report_publications(
            session, "already-removed-report", "lost"
        )

        assert len(requests) == 3
        assert {
            request[0].rsplit("/", 1)[-1]
            for request in requests
        } == {"facebook-post", "instagram-post", "already-removed-post"}
        assert failures == [
            "instagram post could not be removed: Meta instagram post deletion failed "
            "(HTTP 403): code 200; Permission denied. Meta did not grant "
            "instagram_manage_contents. If the permission is missing from the "
            "saved login configuration or has not been approved for this app, "
            "update the Meta configuration, approve it if required, and reconnect."
        ]
        assert already_removed_failures == [
            "facebook post could not be removed: Meta facebook post deletion failed "
            "(HTTP 404): code 100; Post not found"
        ]
        assert session.get(SocialPublication, "facebook-publication") is None
        failed_publication = session.get(SocialPublication, "instagram-publication")
        assert failed_publication is not None
        assert failed_publication.status == "failed"
        assert "Permission denied" in failed_publication.last_error
        already_removed_publication = session.get(
            SocialPublication, "already-removed-publication"
        )
        assert already_removed_publication is not None
        assert already_removed_publication.status == "failed"


def test_meta_post_deletion_requires_explicit_success_confirmation(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")

    class FakeResponse:
        is_error = False
        status_code = 200

        @staticmethod
        def json():
            return {"success": False}

    monkeypatch.setattr(social_publishing.httpx, "delete", lambda *_args, **_kwargs: FakeResponse())
    account = SocialAccount(
        provider="facebook",
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )

    with pytest.raises(RuntimeError, match="did not confirm deletion"):
        social_publishing._delete_meta_post(account, "facebook", "facebook-post")


def test_instagram_delete_permission_error_explains_missing_meta_grant(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(
        social_publishing,
        "_decrypt_token",
        lambda _: "user-token",
    )

    class FakeResponse:
        is_error = True
        status_code = 400

        @staticmethod
        def json():
            return {
                "error": {
                    "code": 200,
                    "message": "Permissions error",
                }
            }

    monkeypatch.setattr(
        social_publishing.httpx,
        "delete",
        lambda *_args, **_kwargs: FakeResponse(),
    )
    account = SocialAccount(
        provider="instagram",
        account_id="instagram-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
        deletion_access_token_encrypted="encrypted-user-token",
    )

    with pytest.raises(RuntimeError) as error:
        social_publishing._delete_meta_post(account, "instagram", "instagram-post")

    assert "instagram_manage_contents" in str(error.value)
    assert "Meta did not grant" in str(error.value)
    assert "approve it if required" in str(error.value)


def test_instagram_delete_uses_saved_facebook_user_access_token(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    decrypted_tokens = {
        "encrypted-page-token": "page-token",
        "encrypted-user-token": "facebook-user-token",
    }
    monkeypatch.setattr(
        social_publishing,
        "_decrypt_token",
        lambda encrypted: decrypted_tokens[encrypted],
    )
    requested = {}

    class FakeResponse:
        is_error = False

        @staticmethod
        def json():
            return {"success": True, "deleted_id": "instagram-post"}

    def fake_delete(url, params, timeout):
        requested.update(url=url, params=params, timeout=timeout)
        return FakeResponse()

    monkeypatch.setattr(social_publishing.httpx, "delete", fake_delete)
    account = SocialAccount(
        provider="instagram",
        account_id="instagram-id",
        account_name="Fendly Instagram",
        access_token_encrypted="encrypted-page-token",
        deletion_access_token_encrypted="encrypted-user-token",
    )

    social_publishing._delete_meta_post(account, "instagram", "instagram-post")

    assert requested["url"].endswith("/instagram-post")
    assert requested["params"] == {"access_token": "facebook-user-token"}


def test_instagram_delete_requires_user_token_for_existing_connections(monkeypatch):
    account = SocialAccount(
        provider="instagram",
        account_id="instagram-id",
        account_name="Fendly Instagram",
        access_token_encrypted="encrypted-page-token",
    )

    with pytest.raises(RuntimeError, match="Facebook User access token"):
        social_publishing._delete_meta_post(account, "instagram", "instagram-post")


def test_facebook_delete_subcode_33_explains_page_permissions_and_post_id(monkeypatch):
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(social_publishing, "_decrypt_token", lambda _: "page-token")

    class FakeResponse:
        is_error = True
        status_code = 400

        @staticmethod
        def json():
            return {
                "error": {
                    "code": 100,
                    "error_subcode": 33,
                    "message": "Unsupported delete request",
                }
            }

    monkeypatch.setattr(
        social_publishing.httpx,
        "delete",
        lambda *_args, **_kwargs: FakeResponse(),
    )
    account = SocialAccount(
        provider="facebook",
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )

    with pytest.raises(RuntimeError) as error:
        social_publishing._delete_meta_post(account, "facebook", "facebook-post")

    assert "pages_manage_posts" in str(error.value)
    assert "content-management task" in str(error.value)
    assert "Page post ID (not a photo ID)" in str(error.value)


def test_meta_oauth_connects_but_warns_when_instagram_delete_permission_is_missing(
    monkeypatch,
    caplog,
):
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
            token = (
                "long-lived-user-token"
                if params.get("grant_type") == "fb_exchange_token"
                else "user-token"
            )
            return FakeResponse({"access_token": token})
        if url.endswith("/debug_token"):
            assert url == "https://graph.facebook.com/v20.0/debug_token"
            assert params == {
                "input_token": "long-lived-user-token",
                "access_token": "meta-app-id|meta-app-secret",
            }
            return FakeResponse(
                {
                    "data": {"scopes": ["pages_manage_posts"]}
                }
            )
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
        instagram_deletion_available = social_publishing._finish_meta_oauth(
            session,
            "auth-code",
            "state",
        )

        assert instagram_deletion_available is False
        assert session.get(SocialAccount, "facebook") is not None
        assert session.get(SocialAccount, "instagram") is not None
    assert "instagram_manage_contents" in caplog.text
    assert "required app approval" in caplog.text


def test_meta_login_url_requests_required_scopes_as_comma_separated_values(monkeypatch):
    factory = _session_factory()
    monkeypatch.setenv("APP_SECRET_KEY", "test-secret-key-that-is-long-enough")
    monkeypatch.setenv("META_APP_ID", "meta-app-id")
    monkeypatch.setenv("META_APP_SECRET", "meta-app-secret")
    monkeypatch.setenv("META_LOGIN_CONFIG_ID", "meta-config-id")
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setenv("META_REDIRECT_URI", "https://api.example.test/api/social/callback/meta")

    with factory() as session:
        result = social_publishing.start_social_connection(session, "admin-user")

    query = parse_qs(urlsplit(result["authorization_url"]).query)
    assert query["scope"] == [
        "public_profile,pages_show_list,pages_read_engagement,pages_manage_posts,"
        "instagram_basic,instagram_content_publish,instagram_manage_contents"
    ]


def test_admin_or_owner_removal_blocks_ambiguous_publication_without_post_id():
    factory = _session_factory()
    with factory() as session:
        publication = SocialPublication(
            id="ambiguous-publication",
            report_id="report-with-unknown-post",
            report_type="lost",
            provider="facebook",
            status="failed",
            last_error=social_publishing.UNEXPECTED_PUBLISHING_FAILURE,
        )
        session.add(publication)
        session.commit()

        failures = social_publishing.remove_report_publications(
            session, "report-with-unknown-post", "lost"
        )

        assert len(failures) == 1
        assert "may have been published without a saved post ID" in failures[0]
        assert session.get(SocialPublication, "ambiguous-publication") is not None


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
