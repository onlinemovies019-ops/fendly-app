import base64
import hashlib
from types import SimpleNamespace

from cryptography.fernet import Fernet

from adoption_social import publish_adoption_listing
from models import AdoptionListing, SocialAccount


def test_opted_in_listing_reuses_connected_meta_accounts_for_both_publications(monkeypatch):
    secret = "test-adoption-social-secret-that-is-long-enough"
    monkeypatch.setenv("APP_SECRET_KEY", secret)
    monkeypatch.delenv("META_FACEBOOK_PAGE_ID", raising=False)
    monkeypatch.delenv("META_INSTAGRAM_ACCOUNT_ID", raising=False)
    monkeypatch.delenv("META_PAGE_ACCESS_TOKEN", raising=False)
    monkeypatch.delenv("APP_DOWNLOAD_URL", raising=False)
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
        account_id="page-id",
        account_name="Fendly",
        access_token_encrypted=token,
    )
    instagram = SocialAccount(
        provider="instagram",
        account_id="instagram-id",
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
    assert requests[0][1]["url"] == "https://storage.example/pet.jpg"
    assert requests[1][1]["image_url"] == "https://storage.example/pet.jpg"
    assert requests[2][1]["creation_id"] == "instagram-container"
