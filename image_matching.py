import asyncio
import io
import threading
from functools import lru_cache

import httpx
from PIL import Image


IMAGE_EMBEDDING_DIMENSIONS = 512
_model_lock = threading.Lock()


@lru_cache(maxsize=1)
def _load_model():
    import open_clip

    model, _, preprocess = open_clip.create_model_and_transforms("ViT-B-32", pretrained="openai")
    model.eval()
    return model, preprocess


def _encode_image(data: bytes) -> list[float] | None:
    try:
        import torch

        with _model_lock:
            model, preprocess = _load_model()
            image = preprocess(Image.open(io.BytesIO(data)).convert("RGB")).unsqueeze(0)
            with torch.inference_mode():
                vector = model.encode_image(image)
                vector = vector / vector.norm(dim=-1, keepdim=True)
            return vector[0].cpu().tolist()
    except Exception:
        return None


async def create_image_embedding(image_url: str | None) -> list[float] | None:
    if not image_url:
        return None
    try:
        async with httpx.AsyncClient(timeout=30) as client:
            response = await client.get(image_url)
            response.raise_for_status()
        return await asyncio.to_thread(_encode_image, response.content)
    except Exception:
        return None


def cosine_similarity(left: list[float] | None, right: list[float] | None) -> float | None:
    if not left or not right or len(left) != len(right):
        return None
    dot = sum(a * b for a, b in zip(left, right))
    left_norm = sum(value * value for value in left) ** 0.5
    right_norm = sum(value * value for value in right) ** 0.5
    if not left_norm or not right_norm:
        return None
    return max(0.0, min(1.0, dot / (left_norm * right_norm)))
