from types import SimpleNamespace

from adoption_social import publish_adoption_listing
from models import AdoptionListing


def test_opted_in_listing_publishes_photo_caption_to_facebook_and_instagram(monkeypatch):
    monkeypatch.setenv("META_FACEBOOK_PAGE_ID", "page-id")
    monkeypatch.setenv("META_INSTAGRAM_ACCOUNT_ID", "instagram-id")
    monkeypatch.setenv("META_PAGE_ACCESS_TOKEN", "page-token")
    monkeypatch.setenv("APP_DOWNLOAD_URL", "https://fendly.example/app")
    monkeypatch.setenv("META_GRAPH_API_VERSION", "v23.0")
    requests = []
    response_ids = iter(("facebook-publication", "instagram-container", "instagram-publication"))

    def fake_post(endpoint, *, data, timeout):
        requests.append((endpoint, data, timeout))
        return SimpleNamespace(raise_for_status=lambda: None, json=lambda: {"id": next(response_ids)})

    monkeypatch.setattr("adoption_social.httpx.post", fake_post)
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
