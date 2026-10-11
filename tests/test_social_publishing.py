import ast
from pathlib import Path

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from sqlalchemy.pool import StaticPool

import main
import social_publishing
from models import Base, LostItem, SocialAccount, SocialPublication


def test_no_social_connection_or_publication_routes_are_registered():
    social_routes = [
        route.path
        for route in main.app.routes
        if route.path.startswith("/api/social")
    ]

    assert social_routes == []


def test_social_cleanup_module_contains_only_meta_get_and_delete_calls():
    source = Path(social_publishing.__file__).read_text(encoding="utf-8")
    tree = ast.parse(source)
    http_methods = {
        node.func.attr.lower()
        for node in ast.walk(tree)
        if isinstance(node, ast.Call)
        and isinstance(node.func, ast.Attribute)
        and isinstance(node.func.value, ast.Name)
        and node.func.value.id == "httpx"
    }

    assert http_methods == {"get", "delete"}
    assert not hasattr(social_publishing, "schedule_report_publications")
    assert not hasattr(social_publishing, "publish_publication")


def test_legacy_meta_post_cleanup_uses_delete_only(monkeypatch):
    monkeypatch.setenv("APP_SECRET_KEY", "test-secret-key-that-is-long-enough")
    encrypted_token = social_publishing.Fernet(
        social_publishing.base64.urlsafe_b64encode(
            social_publishing.hashlib.sha256(
                b"test-secret-key-that-is-long-enough"
            ).digest()
        )
    ).encrypt(b"legacy-token").decode("ascii")
    account = SocialAccount(
        provider="facebook",
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted=encrypted_token,
    )
    calls = []

    class Response:
        is_error = False
        status_code = 200

        @staticmethod
        def json():
            return {"success": True}

    def fake_delete(url, *, params, timeout):
        calls.append((url, params, timeout))
        return Response()

    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    monkeypatch.setattr(social_publishing.httpx, "delete", fake_delete)
    monkeypatch.setattr(
        social_publishing.httpx,
        "post",
        lambda *args, **kwargs: pytest.fail("Social posting is disabled"),
    )

    social_publishing._delete_meta_post(account, "facebook", "page-id_post-id")

    assert calls == [
        (
            "https://graph.facebook.com/v23.0/page-id_post-id",
            {"access_token": "legacy-token"},
            15,
        )
    ]


def test_cleanup_ignores_posts_already_removed_and_preserves_audit_rows(monkeypatch):
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    monkeypatch.setattr(
        social_publishing.httpx,
        "delete",
        lambda *args, **kwargs: pytest.fail("Already-removed posts must not be deleted again"),
    )
    with Session(engine) as session:
        session.add(
            LostItem(
                id="report-1",
                created_by="owner-1",
                title="Lost pet",
                description="Brown dog",
                lat=0,
                lng=0,
                social_share_consent=False,
            )
        )
        session.add(
            SocialPublication(
                id="publication-1",
                report_id="report-1",
                report_type="lost",
                provider="facebook",
                status="removed",
                external_post_id="page-id_post-id",
            )
        )
        session.commit()

        result = social_publishing.remove_legacy_social_posts(session)

        assert result == {
            "processed": 0,
            "removed": 0,
            "remaining": 0,
            "failures": [],
        }
        assert session.get(SocialPublication, "publication-1") is not None
    engine.dispose()
