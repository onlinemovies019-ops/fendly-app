from io import BytesIO

import pytest
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


@pytest.mark.parametrize(
    ("photos", "samples"),
    [
        (
            [Image.new("RGB", (80, 80), color=color) for color in ((220, 40, 40), (40, 80, 220))],
            [((330, 740), (220, 40, 40)), ((750, 740), (40, 80, 220))],
        ),
        (
            [
                Image.new("RGB", (80, 80), color=color)
                for color in ((220, 40, 40), (40, 180, 70), (40, 80, 220))
            ],
            [
                ((300, 740), (220, 40, 40)),
                ((800, 520), (40, 180, 70)),
                ((800, 960), (40, 80, 220)),
            ],
        ),
    ],
)
def test_community_poster_collage_includes_every_report_photo(photos, samples):
    content = render_community_poster(
        title="Milo the dog",
        report_type="LOST",
        category="Pets",
        report_url="https://fendly.app/item/example",
        photo=photos,
        date="Today",
    )

    with Image.open(BytesIO(content)) as poster:
        for point, expected in samples:
            actual = poster.convert("RGB").getpixel(point)
            assert all(abs(actual[channel] - expected[channel]) < 20 for channel in range(3))


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
