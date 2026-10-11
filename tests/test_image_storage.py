import image_storage


def test_private_image_reference_discards_signed_access_token(monkeypatch):
    monkeypatch.setenv("SUPABASE_URL", "https://project.supabase.co")
    signed_url = (
        "https://project.supabase.co/storage/v1/object/sign/uploads/"
        "a1b2c3d4e5f6478899aabbccddeeff00.jpg?token=short-lived"
    )

    assert image_storage.private_image_reference(signed_url) == (
        "storage://uploads/a1b2c3d4e5f6478899aabbccddeeff00.jpg"
    )


def test_private_image_reference_leaves_external_urls_unchanged(monkeypatch):
    monkeypatch.setenv("SUPABASE_URL", "https://project.supabase.co")
    external_url = "https://res.cloudinary.com/fendly/image/upload/legacy.jpg"

    assert image_storage.private_image_reference(external_url) == external_url


def test_refresh_private_image_url_signs_stable_storage_reference(monkeypatch):
    monkeypatch.setenv("SUPABASE_URL", "https://project.supabase.co")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "server-only-key")
    calls = []

    class Response:
        def raise_for_status(self):
            pass

        @staticmethod
        def json():
            return {
                "signedURL": (
                    "/object/sign/uploads/a1b2c3d4e5f6478899aabbccddeeff00.jpg"
                    "?token=refreshed"
                )
            }

    def fake_post(url, **kwargs):
        calls.append((url, kwargs))
        return Response()

    monkeypatch.setattr(image_storage.httpx, "post", fake_post)

    result = image_storage.refresh_private_image_url(
        "storage://uploads/a1b2c3d4e5f6478899aabbccddeeff00.jpg"
    )

    assert result == (
        "https://project.supabase.co/storage/v1/object/sign/uploads/"
        "a1b2c3d4e5f6478899aabbccddeeff00.jpg?token=refreshed"
    )
    assert calls[0][0] == (
        "https://project.supabase.co/storage/v1/object/sign/uploads/"
        "a1b2c3d4e5f6478899aabbccddeeff00.jpg"
    )
    assert calls[0][1]["json"] == {"expiresIn": image_storage.SIGNED_URL_TTL_SECONDS}
