from types import SimpleNamespace

import private_image_migration as migration


def test_cloudinary_variants_group_by_public_asset(monkeypatch):
    monkeypatch.setenv("CLOUDINARY_CLOUD_NAME", "fendly")
    first = (
        "https://res.cloudinary.com/fendly/image/upload/c_fill,w_300/"
        "v1234567890/reports/lost-item.jpg"
    )
    second = (
        "https://res.cloudinary.com/fendly/image/upload/q_auto/"
        "v1234567890/reports/lost-item.jpg"
    )

    assets, unsupported = migration._collect_images(
        [(object(), "image_url", first), (object(), "image_url", second)]
    )

    assert len(assets) == 1
    assert next(iter(assets.values())).locator == "fendly/reports/lost-item"
    assert next(iter(assets.values())).urls == {first, second}
    assert unsupported == 0


def test_local_upload_sources_are_limited_to_configured_host_and_name(monkeypatch):
    monkeypatch.setenv("PUBLIC_BASE_URL", "https://fendly-api.example")
    filename = "a1b2c3d4e5f6478899aabbccddeeff00.jpg"

    assert migration._local_upload_source(
        f"https://fendly-api.example/static/uploads/{filename}"
    ) == filename
    assert migration._local_upload_source(
        f"https://other.example/static/uploads/{filename}"
    ) is None
    assert migration._local_upload_source(
        "https://fendly-api.example/static/uploads/../../private.jpg"
    ) is None


def test_replace_references_updates_scalar_and_image_list(monkeypatch):
    old_url = "https://res.cloudinary.com/fendly/image/upload/v1/item.jpg"
    new_url = "storage://uploads/private.jpg"
    user = SimpleNamespace(profile_photo_url=old_url)
    report = SimpleNamespace(image_urls=[old_url, "keep.jpg"])
    monkeypatch.setattr(
        migration,
        "_all_image_references",
        lambda _session: [
            (user, "profile_photo_url", old_url),
            (report, "image_urls", old_url),
            (report, "image_urls", "keep.jpg"),
        ],
    )

    replaced = migration._replace_references(object(), {old_url}, new_url)

    assert replaced == 2
    assert user.profile_photo_url == new_url
    assert report.image_urls == [new_url, "keep.jpg"]
