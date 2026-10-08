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
from PIL import Image

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


def test_external_post_refresh_reports_available_and_unavailable(monkeypatch):
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
        FakeResponse(200, {"id": "existing-post"}),
        FakeResponse(400, {"error": {"code": 100, "error_subcode": 33}}),
    ])
    calls = []

    def fake_get(url, params, timeout):
        calls.append((url, params, timeout))
        return next(responses)

    monkeypatch.setattr(social_publishing.httpx, "get", fake_get)
    account = SocialAccount(
        provider="facebook",
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted="encrypted-token",
    )

    assert social_publishing._check_external_post(account, "existing-post") == "available"
    assert social_publishing._check_external_post(account, "deleted-post") == "unavailable"
    assert calls[0][0] == "https://graph.facebook.com/v23.0/existing-post"
    assert calls[0][1] == {"fields": "id", "access_token": "page-token"}


def test_publication_refresh_checks_recent_published_posts(monkeypatch):
    factory = _session_factory()
    checked = []
    monkeypatch.setattr(
        social_publishing,
        "_check_external_post",
        lambda account, post_id: checked.append((account.provider, post_id)) or "unavailable",
    )
    with factory() as session:
        account = SocialAccount(
            provider="instagram",
            account_id="instagram-id",
            account_name="Fendly",
            access_token_encrypted="encrypted-token",
        )
        publication = SocialPublication(
            id="publication-refresh",
            report_id="report-1",
            report_type="lost",
            provider="instagram",
            status="published",
            external_post_id="deleted-instagram-post",
        )
        legacy_x_publication = SocialPublication(
            id="legacy-x-publication",
            report_id="report-2",
            report_type="lost",
            provider="x",
            status="failed",
            external_post_id="legacy-x-post",
        )
        session.add_all([account, publication, legacy_x_publication])
        session.commit()

        result = social_publishing.refresh_social_publications(
            limit=20,
            session=session,
            _="admin-uid",
        )

    assert checked == [("instagram", "deleted-instagram-post")]
    assert [item["provider"] for item in result] == ["instagram"]
    assert result[0]["platform_status"] == "unavailable"
    assert result[0]["status"] == "published"


def test_publication_list_excludes_legacy_x_jobs():
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
