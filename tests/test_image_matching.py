from io import BytesIO
from unittest.mock import Mock

import pytest
from fastapi import HTTPException, UploadFile
from PIL import Image
from sqlalchemy.dialects import postgresql
from starlette.datastructures import Headers

import image_matching
from routers import items as items_module


class FakeResponse:
    def __init__(self, embedding):
        self.embedding = embedding

    def raise_for_status(self):
        return None

    def json(self):
        return {"embedding": self.embedding}


class FakeAsyncClient:
    def __init__(self, response, calls):
        self.response = response
        self.calls = calls

    async def __aenter__(self):
        return self

    async def __aexit__(self, exc_type, exc, traceback):
        return None

    async def post(self, url, **kwargs):
        self.calls.append((url, kwargs))
        return self.response


@pytest.mark.asyncio
async def test_remote_image_embedding_uses_signed_url_and_normalizes_vector(monkeypatch):
    embedding = [3.0] + [0.0] * 511
    calls = []
    monkeypatch.setenv(
        "IMAGE_MATCHING_FUNCTION_URL",
        "https://us-central1-example.cloudfunctions.net/generateReportImageEmbedding",
    )
    monkeypatch.setenv("IMAGE_MATCHING_API_KEY", "server-only-key")
    monkeypatch.delenv("USE_LOCAL_IMAGE_EMBEDDING_MODEL", raising=False)
    monkeypatch.setattr(image_matching, "refresh_private_image_url", lambda _: "https://signed.example/photo")
    monkeypatch.setattr(
        image_matching.httpx,
        "AsyncClient",
        lambda **_: FakeAsyncClient(FakeResponse(embedding), calls),
    )

    result = await image_matching.create_image_embedding("storage://report-images/photo.jpg")

    assert result == [1.0] + [0.0] * 511
    assert calls[0][0].endswith("generateReportImageEmbedding")
    assert calls[0][1]["json"] == {"imageUrl": "https://signed.example/photo"}
    assert calls[0][1]["headers"]["x-fendly-image-matching-key"] == "server-only-key"


@pytest.mark.asyncio
@pytest.mark.parametrize(
    "embedding",
    [
        [1.0, 2.0],
        [float("nan")] + [0.0] * 511,
        [0.0] * 512,
    ],
)
async def test_remote_image_embedding_rejects_invalid_vectors(monkeypatch, embedding):
    monkeypatch.setenv("IMAGE_MATCHING_FUNCTION_URL", "https://example.test/embed")
    monkeypatch.setenv("IMAGE_MATCHING_API_KEY", "server-only-key")
    monkeypatch.delenv("USE_LOCAL_IMAGE_EMBEDDING_MODEL", raising=False)
    monkeypatch.setattr(image_matching, "refresh_private_image_url", lambda _: "https://signed.example/photo")
    monkeypatch.setattr(
        image_matching.httpx,
        "AsyncClient",
        lambda **_: FakeAsyncClient(FakeResponse(embedding), []),
    )

    assert await image_matching.create_image_embedding("storage://report-images/photo.jpg") is None


def _jpeg_upload() -> UploadFile:
    image_buffer = BytesIO()
    Image.new("RGB", (8, 8), color="red").save(image_buffer, format="JPEG")
    image_buffer.seek(0)
    return UploadFile(
        file=image_buffer,
        filename="query.jpg",
        headers=Headers({"content-type": "image/jpeg"}),
    )


@pytest.mark.asyncio
async def test_upload_visual_search_returns_pgvector_matches(monkeypatch):
    monkeypatch.setattr(items_module, "local_image_embeddings_enabled", lambda: True)
    monkeypatch.setattr(
        items_module,
        "create_image_embedding_from_bytes",
        lambda data: [1.0] + [0.0] * 511,
    )
    session = Mock()
    session.scalars.return_value.all.return_value = []
    session.execute.return_value.all.return_value = [
        ("found-report-1", 0.12),
        ("found-report-2", 0.28),
    ]

    result = await items_module.visual_search_uploaded_image(
        "lost",
        _jpeg_upload(),
        session=session,
        uid="signed-in-user",
    )

    assert result == {
        "matches": [
            {"report_id": "found-report-1", "similarity": pytest.approx(0.88)},
            {"report_id": "found-report-2", "similarity": pytest.approx(0.72)},
        ]
    }
    sql = str(session.execute.call_args.args[0].compile(dialect=postgresql.dialect()))
    assert "<=>" in sql
    assert "image_embedding_model" in sql
    assert "LIMIT" in sql


@pytest.mark.asyncio
async def test_upload_visual_search_reindexes_stale_photos(monkeypatch):
    model_id = "MobileCLIP-S1:test:v1"
    vector = [1.0] + [0.0] * 511
    stale_report = Mock(
        image_embedding=None,
        image_embedding_model=None,
        image_url="storage://report-images/stale.jpg",
    )
    monkeypatch.setattr(items_module, "local_image_embeddings_enabled", lambda: True)
    monkeypatch.setattr(items_module, "image_embedding_model_id", lambda: model_id)
    monkeypatch.setattr(
        items_module,
        "create_image_embedding_from_bytes",
        lambda data: vector,
    )

    async def create_embedding(_):
        return vector

    monkeypatch.setattr(items_module, "create_image_embedding", create_embedding)
    session = Mock()
    session.scalars.return_value.all.return_value = [stale_report]
    session.execute.return_value.all.return_value = [("stale-report", 0.1)]

    result = await items_module.visual_search_uploaded_image(
        "lost",
        _jpeg_upload(),
        session=session,
        uid="signed-in-user",
    )

    assert stale_report.image_embedding == vector
    assert stale_report.image_embedding_model == model_id
    session.commit.assert_called_once()
    assert result["matches"][0]["report_id"] == "stale-report"


@pytest.mark.asyncio
async def test_upload_visual_search_reports_missing_local_dependencies(monkeypatch):
    monkeypatch.setattr(items_module, "local_image_embeddings_enabled", lambda: True)

    def unavailable_model(_):
        raise RuntimeError("OpenCLIP is not installed")

    monkeypatch.setattr(items_module, "create_image_embedding_from_bytes", unavailable_model)

    with pytest.raises(HTTPException) as error:
        await items_module.visual_search_uploaded_image(
            "lost",
            _jpeg_upload(),
            session=Mock(),
            uid="signed-in-user",
        )

    assert error.value.status_code == 503
    assert "model could not run" in error.value.detail
