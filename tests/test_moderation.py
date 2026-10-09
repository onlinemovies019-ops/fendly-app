import base64

import httpx
import pytest
from fastapi import HTTPException

import moderation


@pytest.mark.asyncio
async def test_moderation_checks_text_and_every_image(monkeypatch):
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    requests = []

    def respond(request):
        requests.append(request)
        return httpx.Response(200, json={"results": [{"flagged": False}]})

    original_client = httpx.AsyncClient

    def mock_client(*, timeout):
        return original_client(transport=httpx.MockTransport(respond), timeout=timeout)

    monkeypatch.setattr(moderation.httpx, "AsyncClient", mock_client)

    result = await moderation.moderate_content(
        "Lost wallet",
        "Blue wallet near the station",
        image_urls=["https://images.example/one.jpg", "https://images.example/two.jpg"],
    )

    assert result is None
    payload = requests[0].read().decode()
    assert '"Lost wallet\\nBlue wallet near the station"' in payload
    assert payload.count('"type":"image_url"') == 2


@pytest.mark.asyncio
async def test_moderation_checks_uploaded_image_bytes(monkeypatch):
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    requests = []

    def respond(request):
        requests.append(request)
        return httpx.Response(200, json={"results": [{"flagged": False}]})

    original_client = httpx.AsyncClient

    def mock_client(*, timeout):
        return original_client(transport=httpx.MockTransport(respond), timeout=timeout)

    monkeypatch.setattr(moderation.httpx, "AsyncClient", mock_client)
    image = b"small-image-content"

    result = await moderation.moderate_content(
        "",
        "",
        image_data=image,
        image_content_type="image/png",
    )

    assert result is None
    payload = requests[0].read().decode()
    assert base64.b64encode(image).decode() in payload
    assert "data:image/png;base64," in payload


@pytest.mark.asyncio
async def test_flagged_content_is_rejected(monkeypatch):
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")

    def respond(_request):
        return httpx.Response(200, json={"results": [{"flagged": True}]})

    original_client = httpx.AsyncClient
    monkeypatch.setattr(
        moderation.httpx,
        "AsyncClient",
        lambda *, timeout: original_client(transport=httpx.MockTransport(respond), timeout=timeout),
    )

    assert await moderation.moderate_content("unsafe content", "") == (
        "Content did not pass safety moderation"
    )


@pytest.mark.asyncio
async def test_moderation_fails_closed_without_provider_credentials(monkeypatch):
    monkeypatch.delenv("OPENAI_API_KEY", raising=False)

    with pytest.raises(HTTPException) as error:
        await moderation.moderate_content("A report", "Details")

    assert error.value.status_code == 503


@pytest.mark.asyncio
async def test_moderation_provider_failure_does_not_use_keyword_fallback(monkeypatch):
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")

    def respond(_request):
        return httpx.Response(500)

    original_client = httpx.AsyncClient
    monkeypatch.setattr(
        moderation.httpx,
        "AsyncClient",
        lambda *, timeout: original_client(transport=httpx.MockTransport(respond), timeout=timeout),
    )

    with pytest.raises(HTTPException) as error:
        await moderation.moderate_content("ordinary report", "No unsafe words")

    assert error.value.status_code == 503
