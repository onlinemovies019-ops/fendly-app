import asyncio
import hashlib
import io
import logging
import math
import os
import tempfile
import threading
from functools import lru_cache
from importlib import import_module
from pathlib import Path
from typing import Mapping, Protocol, Sequence, cast

import httpx
import numpy as np
from PIL import Image, ImageOps

from image_storage import refresh_private_image_url

IMAGE_EMBEDDING_DIMENSIONS = 512
MAX_IMAGE_PIXELS = 25_000_000
LOCAL_IMAGE_EMBEDDING_MODEL_ID = "MobileCLIP-S1:Xenova-ONNX-fp32:v1"
MODEL_REVISION = "48c0a9f89ff272544d1f2595483011a183fd1a52"
MODEL_SHA256 = "5dece7da38f907d440f91c86cca841919c5a1affd2329ca0e94f8593fd0bfbfb"
MODEL_URL = (
    "https://huggingface.co/Xenova/mobileclip_s1/resolve/"
    f"{MODEL_REVISION}/onnx/vision_model.onnx"
)
_model_lock = threading.Lock()
logger = logging.getLogger(__name__)


class _ModelInput(Protocol):
    name: str


class _ModelOutput(Protocol):
    name: str
    shape: Sequence[int | str | None]


class _InferenceSession(Protocol):
    def get_inputs(self) -> Sequence[_ModelInput]: ...

    def get_outputs(self) -> Sequence[_ModelOutput]: ...

    def run(
        self,
        output_names: Sequence[str],
        input_feed: Mapping[str, np.ndarray],
    ) -> Sequence[np.ndarray]: ...


class _SessionOptions(Protocol):
    intra_op_num_threads: int
    inter_op_num_threads: int
    execution_mode: object


class _ExecutionModes(Protocol):
    ORT_SEQUENTIAL: object


class _OnnxRuntime(Protocol):
    ExecutionMode: _ExecutionModes

    def SessionOptions(self) -> _SessionOptions: ...

    def InferenceSession(
        self,
        path: str,
        sess_options: _SessionOptions,
        providers: Sequence[str],
    ) -> _InferenceSession: ...


def image_embeddings_enabled() -> bool:
    if local_image_embeddings_enabled():
        return True
    return bool(
        os.getenv("IMAGE_MATCHING_FUNCTION_URL")
        and os.getenv("IMAGE_MATCHING_API_KEY")
    )


def local_image_embeddings_enabled() -> bool:
    return os.getenv("USE_LOCAL_IMAGE_EMBEDDING_MODEL", "false").lower() == "true"


def image_embedding_model_id() -> str:
    if local_image_embeddings_enabled():
        return LOCAL_IMAGE_EMBEDDING_MODEL_ID
    return "ViT-B-32:openai:v1"


def _download_model() -> Path:
    model_path = Path(tempfile.gettempdir()) / f"fendly-mobileclip-s1-{MODEL_REVISION[:12]}.onnx"
    if model_path.exists():
        digest = hashlib.sha256()
        with model_path.open("rb") as cached_model:
            for chunk in iter(lambda: cached_model.read(1024 * 1024), b""):
                digest.update(chunk)
        if digest.hexdigest() == MODEL_SHA256:
            return model_path
        model_path.unlink()

    temporary_path = model_path.with_suffix(".onnx.tmp")
    digest = hashlib.sha256()
    try:
        with httpx.stream("GET", MODEL_URL, follow_redirects=True, timeout=240) as response:
            response.raise_for_status()
            with temporary_path.open("wb") as model_file:
                for chunk in response.iter_bytes():
                    model_file.write(chunk)
                    digest.update(chunk)
        if digest.hexdigest() != MODEL_SHA256:
            raise RuntimeError("Downloaded visual-search model failed its SHA-256 check")
        temporary_path.replace(model_path)
    finally:
        temporary_path.unlink(missing_ok=True)
    return model_path


@lru_cache(maxsize=1)
def _load_model() -> tuple[_InferenceSession, str, str]:
    try:
        runtime_module = import_module("onnxruntime")
    except ImportError as exc:
        raise RuntimeError("Local visual search requires the onnxruntime package") from exc

    runtime = cast(_OnnxRuntime, runtime_module)
    model_path = _download_model()
    options = runtime.SessionOptions()
    options.intra_op_num_threads = 1
    options.inter_op_num_threads = 1
    options.execution_mode = runtime.ExecutionMode.ORT_SEQUENTIAL
    session = runtime.InferenceSession(
        str(model_path),
        sess_options=options,
        providers=["CPUExecutionProvider"],
    )
    image_input = next(
        (input_info for input_info in session.get_inputs() if input_info.name == "pixel_values"),
        None,
    )
    embedding_output = next(
        (
            output_info
            for output_info in session.get_outputs()
            if len(output_info.shape) == 2
            and output_info.shape[-1] == IMAGE_EMBEDDING_DIMENSIONS
        ),
        None,
    )
    if image_input is None or embedding_output is None:
        raise RuntimeError("Downloaded visual-search model has an unsupported input/output shape")
    return session, image_input.name, embedding_output.name


def _normalize_embedding(values: object) -> list[float]:
    if not isinstance(values, list):
        raise ValueError("Image matching service returned an invalid embedding")
    embedding_values = cast(list[object], values)
    if len(embedding_values) != IMAGE_EMBEDDING_DIMENSIONS:
        raise ValueError("Image matching service returned an invalid embedding")
    embedding: list[float] = []
    for value in embedding_values:
        if isinstance(value, bool) or not isinstance(value, (int, float)):
            raise ValueError("Image matching service returned a non-numeric embedding")
        numeric_value = float(value)
        if not math.isfinite(numeric_value):
            raise ValueError("Image matching service returned a non-finite embedding")
        embedding.append(numeric_value)
    norm = math.sqrt(sum(value * value for value in embedding))
    if norm == 0:
        raise ValueError("Image matching service returned an empty embedding")
    return [value / norm for value in embedding]


def _encode_image(data: bytes) -> list[float] | None:
    try:
        return create_image_embedding_from_bytes(data)
    except Exception:
        logger.exception("Local CLIP model could not create an image embedding")
        return None


def create_image_embedding_from_bytes(data: bytes) -> list[float]:
    if not data:
        raise ValueError("An image is required to create an embedding")

    try:
        with _model_lock:
            session, input_name, output_name = _load_model()
            with Image.open(io.BytesIO(data)) as source_image:
                if source_image.width * source_image.height > MAX_IMAGE_PIXELS:
                    raise ValueError("Image dimensions exceed the visual search limit")
                image = ImageOps.fit(
                    source_image.convert("RGB"),
                    (256, 256),
                    method=Image.Resampling.BILINEAR,
                    centering=(0.5, 0.5),
                )
                pixels = np.asarray(image, dtype=np.float32) / 255.0
            input_tensor = np.ascontiguousarray(pixels.transpose(2, 0, 1)[None, ...])
            vector = np.asarray(
                session.run([output_name], {input_name: input_tensor})[0],
                dtype=np.float32,
            ).reshape(-1)
            norm = float(np.linalg.norm(vector))
            if not math.isfinite(norm) or norm == 0:
                raise RuntimeError("Local CLIP model returned an invalid image vector")
            embedding = [float(value) for value in vector / norm]
    except Exception as exc:
        raise RuntimeError("Local CLIP model could not analyze this image") from exc

    if len(embedding) != IMAGE_EMBEDDING_DIMENSIONS:
        raise RuntimeError("Local CLIP model returned an unexpected vector size")
    if any(not math.isfinite(value) for value in embedding):
        raise RuntimeError("Local CLIP model returned an invalid image vector")
    return embedding


async def create_image_embedding(image_url: str | None) -> list[float] | None:
    if not image_url:
        return None
    try:
        image_url = await asyncio.to_thread(refresh_private_image_url, image_url)
        if not image_url:
            return None
        if local_image_embeddings_enabled():
            async with httpx.AsyncClient(timeout=15) as client:
                response = await client.get(image_url)
                response.raise_for_status()
                data = response.content
            return await asyncio.to_thread(_encode_image, data)

        function_url = os.getenv("IMAGE_MATCHING_FUNCTION_URL", "").strip()
        api_key = os.getenv("IMAGE_MATCHING_API_KEY", "")
        if not function_url or not api_key:
            return None
        async with httpx.AsyncClient(timeout=httpx.Timeout(240, connect=15)) as client:
            response = await client.post(
                function_url,
                json={"imageUrl": image_url},
                headers={"x-fendly-image-matching-key": api_key},
            )
            response.raise_for_status()
            payload = response.json()
        values = (
            cast(dict[str, object], payload).get("embedding")
            if isinstance(payload, dict)
            else None
        )
        return _normalize_embedding(values)
    except (httpx.HTTPError, RuntimeError, ValueError, TypeError, KeyError):
        logger.exception("Image matching service could not create an image embedding")
        return None


def cosine_similarity(left: list[float] | None, right: list[float] | None) -> float | None:
    if left is None or right is None or len(left) == 0 or len(left) != len(right):
        return None
    dot = sum(a * b for a, b in zip(left, right))
    left_norm = sum(value * value for value in left) ** 0.5
    right_norm = sum(value * value for value in right) ** 0.5
    if not left_norm or not right_norm:
        return None
    return max(0.0, min(1.0, dot / (left_norm * right_norm)))
