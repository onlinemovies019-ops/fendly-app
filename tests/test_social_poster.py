from io import BytesIO

from PIL import Image

from adoption_poster import render_adoption_poster
from social_poster import POSTER_HEIGHT, POSTER_WIDTH, render_community_poster


def test_community_poster_renders_distinct_lost_and_found_designs():
    photo = Image.new("RGB", (320, 240), color=(120, 145, 160))
    common = {
        "title": "Milo the dog",
        "category": "Pets",
        "report_url": "https://fendly.app/item/example",
        "photo": photo,
        "date": "Today",
    }

    lost = render_community_poster(report_type="LOST", **common)
    found = render_community_poster(report_type="FOUND", **common)

    assert lost != found
    for content in (lost, found):
        with Image.open(BytesIO(content)) as poster:
            assert poster.format == "JPEG"
            assert poster.size == (POSTER_WIDTH, POSTER_HEIGHT)


def test_adoption_poster_renders_a_social_ready_jpeg():
    photo = BytesIO()
    Image.new("RGB", (320, 240), color=(150, 120, 95)).save(photo, format="JPEG")

    content = render_adoption_poster(
        photo.getvalue(),
        pet_name="Milo",
        species="dog",
        breed="Indie",
        age_months=18,
        location_city="Pune",
        health_status="Vaccinated",
        download_url="https://fendly.app",
    )

    with Image.open(BytesIO(content)) as poster:
        assert poster.format == "JPEG"
        assert poster.size == (1080, 1350)
