import base64
import hashlib
from urllib.parse import parse_qs, urlsplit

import httpx
import pytest
from cryptography.fernet import Fernet
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, select
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

import database
import main
from models import Base, SocialAccount, SocialOAuthState
from routers import admin as admin_module
from routers import social_accounts as social_module


@pytest.fixture
def social_client(monkeypatch):
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    session_factory = sessionmaker(bind=engine, autoflush=False, autocommit=False)

    def override_get_db():
        with session_factory() as session:
            yield session

    uid = ["admin-uid"]
    main.app.dependency_overrides[database.get_db] = override_get_db
    main.app.dependency_overrides[admin_module.get_current_user] = lambda: uid[0]
    monkeypatch.setenv("ADMIN_FIREBASE_UIDS", "admin-uid")
    monkeypatch.setenv("APP_SECRET_KEY", "social-test-secret-long-enough-for-testing")
    monkeypatch.setenv("META_APP_ID", "test-app-id")
    monkeypatch.setenv("META_APP_SECRET", "test-app-secret")
    monkeypatch.setenv("META_LOGIN_CONFIG_ID", "test-config-id")
    monkeypatch.setenv("META_REDIRECT_URI", "https://api.example.com/api/social/callback/meta")
    monkeypatch.setenv("META_PAGE_ID", "1234567890")
    client = TestClient(main.app)
    yield client, session_factory, uid
    client.close()
    main.app.dependency_overrides.pop(database.get_db, None)
    main.app.dependency_overrides.pop(admin_module.get_current_user, None)
    engine.dispose()


def _authorized_start(client: TestClient) -> tuple[str, dict[str, list[str]]]:
    response = client.post("/api/social/connect/meta")
    assert response.status_code == 200
    authorization_url = response.json()["authorization_url"]
    parsed = urlsplit(authorization_url)
    assert parsed.scheme == "https"
    assert parsed.netloc == "www.facebook.com"
    return authorization_url, parse_qs(parsed.query)


def test_social_status_requires_admin(social_client):
    client, _, uid = social_client
    uid[0] = "ordinary-user"

    response = client.get("/api/social/status")

    assert response.status_code == 403


def test_meta_connect_generates_secure_authorization_url_and_state(social_client):
    client, session_factory, _ = social_client

    authorization_url, params = _authorized_start(client)

    assert urlsplit(authorization_url).path == "/dialog/oauth"
    assert params["client_id"] == ["test-app-id"]
    assert params["config_id"] == ["test-config-id"]
    assert params["redirect_uri"] == [
        "https://api.example.com/api/social/callback/meta"
    ]
    assert params["response_type"] == ["code"]
    state = params["state"][0]
    with session_factory() as session:
        stored = session.scalars(select(SocialOAuthState)).one()
        assert stored.provider == "meta"
        assert stored.created_by == "admin-uid"
        assert stored.state_digest == hashlib.sha256(state.encode()).hexdigest()


def test_meta_callback_rejects_invalid_state(social_client):
    client, _, _ = social_client

    response = client.get("/api/social/callback/meta?code=authorization-code&state=invalid")

    assert response.status_code == 400
    assert "state is invalid or expired" in response.json()["detail"]


def test_meta_callback_exchanges_tokens_discovers_page_and_saves_encrypted_accounts(
    social_client, monkeypatch
):
    client, session_factory, _ = social_client
    _, params = _authorized_start(client)
    requests = []
    responses = iter([
        {"access_token": "short-user-token"},
        {"access_token": "long-lived-user-token"},
        {"data": {"scopes": [
            "pages_manage_posts",
            "instagram_basic",
            "instagram_content_publish",
        ]}},
        {"data": [{
            "id": "1234567890",
            "name": "Fendly Page",
            "access_token": "fresh-page-token",
            "instagram_business_account": {
                "id": "9876543210",
                "username": "fendlypets",
            },
        }]},
    ])

    class FakeResponse:
        status_code = 200
        is_success = True

        def __init__(self, payload):
            self.payload = payload

        def json(self):
            return self.payload

    def fake_get(url, *, params, timeout):
        requests.append((url, params, timeout))
        return FakeResponse(next(responses))

    monkeypatch.setattr(social_module.httpx, "get", fake_get)

    response = client.get(
        "/api/social/callback/meta",
        params={"code": "authorization-code", "state": params["state"][0]},
    )

    assert response.status_code == 200
    assert "Meta reconnected successfully" in response.text
    assert len(requests) == 4
    assert requests[0][1]["code"] == "authorization-code"
    assert requests[1][1]["fb_exchange_token"] == "short-user-token"
    assert requests[2][1]["input_token"] == "long-lived-user-token"
    assert requests[3][1]["access_token"] == "long-lived-user-token"
    key = base64.urlsafe_b64encode(
        hashlib.sha256(
            b"social-test-secret-long-enough-for-testing"
        ).digest()
    )
    fernet = Fernet(key)
    with session_factory() as session:
        accounts = {
            account.provider: account
            for account in session.scalars(select(SocialAccount)).all()
        }
        assert set(accounts) == {"facebook", "instagram"}
        for provider, account_id in (
            ("facebook", "1234567890"),
            ("instagram", "9876543210"),
        ):
            assert accounts[provider].account_id == account_id
            assert accounts[provider].access_token_encrypted != "fresh-page-token"
            assert fernet.decrypt(
                accounts[provider].access_token_encrypted.encode()
            ).decode() == "fresh-page-token"
            assert fernet.decrypt(
                accounts[provider].deletion_access_token_encrypted.encode()
            ).decode() == "long-lived-user-token"
        assert session.scalars(select(SocialOAuthState)).all() == []


def test_meta_callback_state_cannot_be_reused(social_client, monkeypatch):
    client, _, _ = social_client
    _, params = _authorized_start(client)
    callback_params = {"code": "authorization-code", "state": params["state"][0]}

    def fail_exchange(*_args, **_kwargs):
        raise httpx.ConnectError("test network failure")

    monkeypatch.setattr(social_module.httpx, "get", fail_exchange)
    first_response = client.get("/api/social/callback/meta", params=callback_params)
    second_response = client.get("/api/social/callback/meta", params=callback_params)

    assert first_response.status_code == 502
    assert second_response.status_code == 400
