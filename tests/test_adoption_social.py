import base64
import hashlib
import logging
from types import SimpleNamespace

from cryptography.fernet import Fernet
import httpx

from adoption_social import publish_adoption_listing
from models import AdoptionListing, SocialAccount


def test_opted_in_listing_reuses_connected_meta_accounts_for_both_publications(monkeypatch):
    secret = "test-adoption-social-secret-that-is-long-enough"
    monkeypatch.setenv("APP_SECRET_KEY", secret)
    monkeypatch.delenv("META_FACEBOOK_PAGE_ID", raising=False)
    monkeypatch.delenv("META_INSTAGRAM_ACCOUNT_ID", raising=False)
    monkeypatch.delenv("META_PAGE_ACCESS_TOKEN", raising=False)
    monkeypatch.delenv("APP_DOWNLOAD_URL", raising=False)
    monkeypatch.setenv("META_PAGE_ID", "1111111111")
    monkeypatch.setenv("META_INSTAGRAM_ACCOUNT_ID", "2222222222")
    monkeypatch.setenv("META_PAGE_ACCESS_TOKEN", "incorrect-direct-token")
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    requests = []
    response_ids = iter(("facebook-publication", "instagram-container", "instagram-publication"))

    def fake_post(endpoint, *, data, timeout):
        requests.append((endpoint, data, timeout))
        return SimpleNamespace(raise_for_status=lambda: None, json=lambda: {"id": next(response_ids)})

    monkeypatch.setattr("adoption_social.httpx.post", fake_post)
    fernet_key = base64.urlsafe_b64encode(hashlib.sha256(secret.encode("utf-8")).digest())
    token = Fernet(fernet_key).encrypt(b"page-token").decode("ascii")
    facebook = SocialAccount(
        provider="facebook",
        account_id="1234567890",
        account_name="Fendly",
        access_token_encrypted=token,
    )
    instagram = SocialAccount(
        provider="instagram",
        account_id="9876543210",
        account_name="Fendly Instagram",
        access_token_encrypted=token,
    )
    listing = AdoptionListing(
        id="listing-1",
        lister_user_id="owner",
        pet_name="Milo",
        species="dog",
        breed="Indie",
        age_months=18,
        gender="male",
        health_status="Vaccinated",
        description="Friendly and active.",
        location_city="Pune",
        images=["https://storage.example/pet.jpg"],
        auto_post_social=True,
    )
    recorded = []

    publications, errors = publish_adoption_listing(
        listing,
        lambda provider, external_id: recorded.append((provider, external_id)),
        facebook_account=facebook,
        instagram_account=instagram,
    )

    assert errors == []
    assert publications == {
        "facebook": "facebook-publication",
        "instagram": "instagram-publication",
    }
    assert recorded == [
        ("facebook", "facebook-publication"),
        ("instagram", "instagram-publication"),
    ]
    assert len(requests) == 3
    assert all("Milo" in request[1].get("caption", "") for request in requests[:2])
    assert requests[0][0].endswith("/1234567890/photos")
    assert requests[0][1]["access_token"] == "page-token"
    assert requests[1][0].endswith("/9876543210/media")
    assert requests[1][1]["access_token"] == "page-token"
    assert requests[0][1]["url"] == "https://storage.example/pet.jpg"
    assert requests[1][1]["image_url"] == "https://storage.example/pet.jpg"
    assert requests[2][1]["creation_id"] == "instagram-container"


def test_meta_api_failures_include_graph_error_in_render_logs(monkeypatch, caplog):
    monkeypatch.delenv("META_FACEBOOK_PAGE_ID", raising=False)
    monkeypatch.setenv("META_PAGE_ID", "1234567890")
    monkeypatch.setenv("META_INSTAGRAM_ACCOUNT_ID", "9876543210")
    monkeypatch.setenv("META_PAGE_ACCESS_TOKEN", "test-token")

    def fail_post(endpoint, *, data, timeout):
        request = httpx.Request("POST", endpoint)
        response = httpx.Response(
            400,
            request=request,
            json={"error": {"message": "Meta permission denied"}},
        )
        raise httpx.HTTPStatusError(
            "Meta request failed",
            request=request,
            response=response,
        )

    monkeypatch.setattr("adoption_social.httpx.post", fail_post)
    listing = AdoptionListing(
        id="listing-2",
        lister_user_id="owner",
        pet_name="Milo",
        species="dog",
        breed="",
        age_months=18,
        gender="male",
        health_status="Vaccinated",
        description="Friendly and active.",
        location_city="Pune",
        images=["https://storage.example/pet.jpg"],
        auto_post_social=True,
    )

    with caplog.at_level(logging.WARNING, logger="adoption_social"):
        _, errors = publish_adoption_listing(listing, lambda *_: None)

    assert errors == [
        "Facebook: Meta permission denied",
        "Instagram: Meta permission denied",
    ]
    assert "Facebook publishing failed: Meta permission denied" in caplog.text
    assert "Instagram publishing failed: Meta permission denied" in caplog.text


def test_meta_page_id_requires_page_access_token(monkeypatch):
    monkeypatch.setenv("META_PAGE_ID", "1234567890")
    monkeypatch.delenv("META_FACEBOOK_PAGE_ID", raising=False)
    monkeypatch.delenv("META_PAGE_ACCESS_TOKEN", raising=False)

    listing = AdoptionListing(
        id="listing-3",
        lister_user_id="owner",
        pet_name="Milo",
        species="dog",
        breed="",
        age_months=18,
        gender="male",
        health_status="Vaccinated",
        description="Friendly and active.",
        location_city="Pune",
        images=["https://storage.example/pet.jpg"],
        auto_post_social=True,
    )

    _, errors = publish_adoption_listing(listing, lambda *_: None)

    assert errors[0] == (
        "Facebook: META_PAGE_ACCESS_TOKEN is required with the configured Meta account ID"
    )


def test_instagram_account_id_is_discovered_from_page_if_token_was_entered_as_id(monkeypatch):
    monkeypatch.setenv("META_PAGE_ID", "1234567890")
    monkeypatch.setenv("META_INSTAGRAM_ACCOUNT_ID", "EAA-this-is-not-an-account-id")
    monkeypatch.setenv("META_PAGE_ACCESS_TOKEN", "page-access-token")
    requests = []
    response_ids = iter(("facebook-publication", "instagram-container", "instagram-publication"))

    def fake_get(endpoint, *, params, timeout):
        requests.append(("GET", endpoint, params, timeout))
        return SimpleNamespace(
            raise_for_status=lambda: None,
            json=lambda: {"instagram_business_account": {"id": "9876543210"}},
        )

    def fake_post(endpoint, *, data, timeout):
        requests.append(("POST", endpoint, data, timeout))
        return SimpleNamespace(raise_for_status=lambda: None, json=lambda: {"id": next(response_ids)})

    monkeypatch.setattr("adoption_social.httpx.get", fake_get)
    monkeypatch.setattr("adoption_social.httpx.post", fake_post)
    listing = AdoptionListing(
        id="listing-4",
        lister_user_id="owner",
        pet_name="Milo",
        species="dog",
        breed="",
        age_months=18,
        gender="male",
        health_status="Vaccinated",
        description="Friendly and active.",
        location_city="Pune",
        images=["https://storage.example/pet.jpg"],
        auto_post_social=True,
    )

    publications, errors = publish_adoption_listing(listing, lambda *_: None)

    assert errors == []
    assert publications["instagram"] == "instagram-publication"
    discovery = next(request for request in requests if request[0] == "GET")
    assert discovery[0] == "GET"
    assert discovery[1].endswith("/1234567890")
    assert discovery[2]["fields"] == "instagram_business_account{id}"
    instagram_posts = [
        request for request in requests
        if request[0] == "POST" and "/9876543210/" in request[1]
    ]
    assert len(instagram_posts) == 2
    assert all("EAA-this-is-not-an-account-id" not in request[1] for request in requests)


def test_instagram_discovery_redacts_access_token_from_meta_error(monkeypatch):
    token = "EAA-this-secret-token-should-not-appear"
    monkeypatch.setenv("META_PAGE_ID", "1234567890")
    monkeypatch.setenv("META_INSTAGRAM_ACCOUNT_ID", token)
    monkeypatch.setenv("META_PAGE_ACCESS_TOKEN", token)

    def fail_get(endpoint, *, params, timeout):
        request = httpx.Request("GET", endpoint)
        response = httpx.Response(
            400,
            request=request,
            json={"error": {"message": f"Invalid object {token}"}},
        )
        raise httpx.HTTPStatusError("Meta request failed", request=request, response=response)

    monkeypatch.setattr("adoption_social.httpx.get", fail_get)
    listing = AdoptionListing(
        id="listing-5",
        lister_user_id="owner",
        pet_name="Milo",
        species="dog",
        breed="",
        age_months=18,
        gender="male",
        health_status="Vaccinated",
        description="Friendly and active.",
        location_city="Pune",
        images=["https://storage.example/pet.jpg"],
        facebook_post_id="already-published",
        auto_post_social=True,
    )

    _, errors = publish_adoption_listing(listing, lambda *_: None)

    assert token not in " ".join(errors)


def test_facebook_deprecated_publish_actions_error_is_actionable(monkeypatch):
    monkeypatch.setenv("META_PAGE_ID", "1234567890")
    monkeypatch.setenv("META_PAGE_ACCESS_TOKEN", "page-token")
    monkeypatch.delenv("META_FACEBOOK_PAGE_ID", raising=False)

    def fail_post(endpoint, *, data, timeout):
        request = httpx.Request("POST", endpoint)
        response = httpx.Response(
            400,
            request=request,
            json={
                "error": {
                    "message": "The permission(s) publish_actions are not available. It has been deprecated."
                }
            },
        )
        raise httpx.HTTPStatusError("Meta request failed", request=request, response=response)

    monkeypatch.setattr("adoption_social.httpx.post", fail_post)
    listing = AdoptionListing(
        id="listing-6",
        lister_user_id="owner",
        pet_name="Milo",
        species="dog",
        breed="",
        age_months=18,
        gender="male",
        health_status="Vaccinated",
        description="Friendly and active.",
        location_city="Pune",
        images=["https://storage.example/pet.jpg"],
        auto_post_social=True,
    )

    _, errors = publish_adoption_listing(listing, lambda *_: None)

    assert errors[0] == (
        "Facebook: Meta rejected Facebook publishing because publish_actions is deprecated. "
        "Use a Facebook Page access token with pages_manage_posts granted to the Meta app and the Page task."
    )
