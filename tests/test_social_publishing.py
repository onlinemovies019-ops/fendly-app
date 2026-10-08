import asyncio
import hashlib
import time
from unittest.mock import AsyncMock
from urllib.parse import parse_qs, urlsplit

import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool
from fastapi import BackgroundTasks
from fastapi import HTTPException

import social_publishing
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


def test_oauth_state_is_consumed_without_losing_pkce_verifier():
    factory = _session_factory()
    raw_state = "single-use-state"
    state_digest = hashlib.sha256(raw_state.encode("utf-8")).hexdigest()
    with factory() as session:
        session.add(
            SocialOAuthState(
                state_digest=state_digest,
                provider="x",
                created_by="admin",
                expires_at=int(time.time()) + 60,
                code_verifier="stored-pkce-verifier",
            )
        )
        session.commit()

        verifier = social_publishing._consume_state(session, raw_state, "x")

        assert verifier == "stored-pkce-verifier"
        assert session.get(SocialOAuthState, state_digest) is None


def test_start_x_connection_creates_pkce_authorization_request(monkeypatch):
    factory = _session_factory()
    monkeypatch.setenv("APP_SECRET_KEY", "test-secret-key-that-is-long-enough")
    monkeypatch.setenv("X_CLIENT_ID", "x-client-id")
    monkeypatch.setenv("X_REDIRECT_URI", "https://api.example.test/api/social/callback/x")

    with factory() as session:
        result = social_publishing.start_social_connection("x", session, "admin-uid")
        parsed = urlsplit(result["authorization_url"])
        params = parse_qs(parsed.query)
        state_digest = hashlib.sha256(params["state"][0].encode("utf-8")).hexdigest()
        oauth_state = session.get(SocialOAuthState, state_digest)

        assert parsed.scheme == "https"
        assert parsed.netloc == "x.com"
        assert params["code_challenge_method"] == ["S256"]
        assert params["scope"] == ["tweet.read tweet.write users.read offline.access"]
        assert oauth_state is not None
        assert oauth_state.provider == "x"
        assert oauth_state.code_verifier is not None


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


def test_x_oauth_callback_exchanges_code_and_stores_refresh_token(monkeypatch):
    factory = _session_factory()
    monkeypatch.setenv("APP_SECRET_KEY", "test-secret-key-that-is-long-enough")
    monkeypatch.setenv("X_CLIENT_ID", "x-client-id")
    monkeypatch.setenv("X_REDIRECT_URI", "https://api.example.test/api/social/callback/x")
    monkeypatch.setattr(
        social_publishing,
        "_consume_state",
        lambda *_: "valid-pkce-verifier",
    )

    class FakeResponse:
        is_error = False

        def __init__(self, value):
            self.value = value

        def json(self):
            return self.value

    def fake_post(url, data, auth, timeout):
        assert url == "https://api.x.com/2/oauth2/token"
        assert data["code_verifier"] == "valid-pkce-verifier"
        return FakeResponse(
            {
                "access_token": "x-access-token",
                "refresh_token": "x-refresh-token",
                "expires_in": 7200,
            }
        )

    def fake_get(url, params, headers, timeout):
        assert url == "https://api.x.com/2/users/me"
        return FakeResponse(
            {"data": {"id": "x-account-id", "username": "fendly_community"}}
        )

    monkeypatch.setattr(social_publishing.httpx, "post", fake_post)
    monkeypatch.setattr(social_publishing.httpx, "get", fake_get)

    with factory() as session:
        social_publishing._finish_x_oauth(session, "auth-code", "state")

        account = session.get(SocialAccount, "x")
        assert account is not None
        assert account.account_id == "x-account-id"
        assert account.account_name == "fendly_community"
        assert social_publishing._decrypt_token(account.access_token_encrypted) == "x-access-token"
        assert account.refresh_token_encrypted is not None
        assert social_publishing._decrypt_token(account.refresh_token_encrypted) == "x-refresh-token"


def test_social_caption_excludes_report_details_and_masks_contact_values():
    report = LostItem(
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
                for provider in ("facebook", "instagram", "x")
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
            ("instagram", "skipped"),
            ("x", "pending"),
        ]
        assert len(tasks.tasks) == 2

        report.social_share_consent = False
        social_publishing.schedule_report_publications(session, report, "found", None)
        assert len(session.scalars(select(SocialPublication)).all()) == 3


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
        job = session.scalar(select(SocialPublication).where(SocialPublication.report_id == report.id))
        assert job is not None
        assert job.provider == "facebook"
        assert job.status == "pending"
        assert len(tasks.tasks) == 1


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
