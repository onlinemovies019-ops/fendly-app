import asyncio
from types import SimpleNamespace

import pytest

import image_storage


class FakeBucket:
    def __init__(self):
        self.uploaded = []
        self.signed = []
        self.signed_batches = []
        self.deleted = []

    def upload(self, path, data, file_options):
        self.uploaded.append((path, data, file_options))
        return {"path": path}

    def create_signed_url(self, path, expires_in):
        self.signed.append((path, expires_in))
        return {"signedURL": f"/object/sign/report-images/{path}?token=short-lived"}

    def create_signed_urls(self, paths, expires_in):
        self.signed_batches.append((paths, expires_in))
        return [
            {
                "signedURL": f"/object/sign/report-images/{path}?token=short-lived",
                "error": None,
            }
            for path in paths
        ]

    def remove(self, paths):
        self.deleted.extend(paths)
        return [{"name": path} for path in paths]

    def download(self, path):
        return b"image-bytes"


class FakeStorage:
    def __init__(self, bucket):
        self.bucket = bucket
        self.requested_buckets = []

    def from_(self, name):
        self.requested_buckets.append(name)
        return self.bucket

    def get_bucket(self, name):
        assert name == "report-images"
        return {"id": name, "public": False}


class FakeClient:
    def __init__(self, bucket):
        self.storage = FakeStorage(bucket)


def configure_storage(monkeypatch):
    monkeypatch.setenv("SUPABASE_URL", "https://project.supabase.co")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "server-only-key")
    bucket = FakeBucket()
    client = FakeClient(bucket)
    monkeypatch.setattr(image_storage, "create_client", lambda *_: client)
    return client, bucket


def test_store_image_uploads_directly_to_private_report_images_bucket(monkeypatch):
    client, bucket = configure_storage(monkeypatch)

    result = asyncio.run(
        image_storage.store_image(b"image-bytes", "report/photo.jpg", "image/jpeg")
    )

    assert bucket.uploaded == [
        (
            "report/photo.jpg",
            b"image-bytes",
            {"content-type": "image/jpeg", "upsert": "false"},
        )
    ]
    assert bucket.signed == [("report/photo.jpg", image_storage.SIGNED_URL_TTL_SECONDS)]
    assert client.storage.requested_buckets == ["report-images", "report-images"]
    assert result == (
        "https://project.supabase.co/storage/v1/object/sign/"
        "report-images/report/photo.jpg?token=short-lived"
    )


def test_private_image_reference_stores_only_the_stable_object_key(monkeypatch):
    monkeypatch.setenv("SUPABASE_URL", "https://project.supabase.co")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "server-only-key")
    signed_url = (
        "https://project.supabase.co/storage/v1/object/sign/report-images/"
        "report/photo.jpg?token=short-lived"
    )

    assert image_storage.private_image_reference(signed_url) == (
        "storage://report-images/report/photo.jpg"
    )


def test_refresh_private_image_url_signs_the_stable_reference(monkeypatch):
    configure_storage(monkeypatch)

    result = image_storage.refresh_private_image_url(
        "storage://report-images/report/photo.jpg"
    )

    assert result == (
        "https://project.supabase.co/storage/v1/object/sign/"
        "report-images/report/photo.jpg?token=short-lived"
    )


def test_refresh_private_image_urls_signs_private_photos_in_one_batch(monkeypatch):
    client, bucket = configure_storage(monkeypatch)

    result = image_storage.refresh_private_image_urls([
        "storage://report-images/lost/photo-one.jpg",
        "https://res.cloudinary.com/fendly/legacy.jpg",
        "storage://report-images/found/photo-two.jpg",
        None,
    ])

    assert bucket.signed_batches == [(
        ["lost/photo-one.jpg", "found/photo-two.jpg"],
        image_storage.SIGNED_URL_TTL_SECONDS,
    )]
    assert client.storage.requested_buckets == ["report-images"]
    assert result == [
        "https://project.supabase.co/storage/v1/object/sign/report-images/"
        "lost/photo-one.jpg?token=short-lived",
        "https://res.cloudinary.com/fendly/legacy.jpg",
        "https://project.supabase.co/storage/v1/object/sign/report-images/"
        "found/photo-two.jpg?token=short-lived",
        None,
    ]


def test_external_image_urls_remain_unchanged_during_legacy_migration(monkeypatch):
    monkeypatch.setenv("SUPABASE_URL", "https://project.supabase.co")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "server-only-key")
    external_url = "https://res.cloudinary.com/fendly/image/upload/legacy.jpg"

    assert image_storage.private_image_reference(external_url) == external_url
    assert image_storage.refresh_private_image_url(external_url) == external_url


def test_legacy_uploads_bucket_can_be_signed_for_migration_compatibility(monkeypatch):
    _, bucket = configure_storage(monkeypatch)

    result = image_storage.refresh_private_image_url(
        "storage://uploads/legacy.jpg"
    )

    assert result.endswith("/object/sign/report-images/legacy.jpg?token=short-lived")
    assert bucket.signed[-1] == ("legacy.jpg", image_storage.SIGNED_URL_TTL_SECONDS)


def test_private_bucket_verification_rejects_public_bucket(monkeypatch):
    client, _ = configure_storage(monkeypatch)
    client.storage.get_bucket = lambda _name: SimpleNamespace(public=True)

    with pytest.raises(RuntimeError, match="must be private"):
        image_storage.verify_private_bucket()
