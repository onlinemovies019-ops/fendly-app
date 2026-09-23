import os
import hashlib
import math
from functools import lru_cache

import httpx


EMBEDDING_DIMENSIONS = 1536
DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small"
LOCAL_EMBEDDING_MODEL = os.getenv("LOCAL_EMBEDDING_MODEL", "sentence-transformers/all-MiniLM-L6-v2")


@lru_cache(maxsize=1)
def _load_local_embedding_model():
    from sentence_transformers import SentenceTransformer

    return SentenceTransformer(LOCAL_EMBEDDING_MODEL)


def _normalize_embedding(values: list[float] | None) -> list[float] | None:
    if values is None:
        return None
    normalized = list(values)
    if len(normalized) > EMBEDDING_DIMENSIONS:
        normalized = normalized[:EMBEDDING_DIMENSIONS]
    if len(normalized) < EMBEDDING_DIMENSIONS:
        normalized.extend([0.0] * (EMBEDDING_DIMENSIONS - len(normalized)))
    return normalized


def item_text(title: str, description: str, category: str) -> str:
    return f"Category: {category}\nTitle: {title}\nDescription: {description}"


def _local_embedding(text: str) -> list[float]:
    vector = [0.0] * EMBEDDING_DIMENSIONS
    for word in text.lower().split():
        digest = hashlib.sha256(word.encode("utf-8")).digest()
        index = int.from_bytes(digest[:4], "big") % EMBEDDING_DIMENSIONS
        vector[index] += 1.0 if digest[4] % 2 else -1.0
    norm = math.sqrt(sum(value * value for value in vector))
    return [value / norm for value in vector] if norm else vector


async def create_embedding(text: str) -> list[float] | None:
    if not text or not text.strip():
        return [0.0] * EMBEDDING_DIMENSIONS

    api_key = os.getenv("OPENAI_API_KEY")
    if api_key:
        endpoint = os.getenv("OPENAI_EMBEDDINGS_URL", "https://api.openai.com/v1/embeddings")
        payload = {
            "input": text,
            "model": os.getenv("OPENAI_EMBEDDING_MODEL", DEFAULT_EMBEDDING_MODEL),
            "dimensions": EMBEDDING_DIMENSIONS,
        }
        try:
            async with httpx.AsyncClient(timeout=30) as client:
                response = await client.post(endpoint, json=payload, headers={"Authorization": f"Bearer {api_key}"})
            response.raise_for_status()
            embedding = response.json()["data"][0]["embedding"]
            if len(embedding) != EMBEDDING_DIMENSIONS:
                raise ValueError("Embedding provider returned an unexpected vector size")
            return embedding
        except (httpx.HTTPError, ValueError, KeyError, IndexError, TypeError):
            return _local_embedding(text)

    # Keep report creation fast on free hosting. The transformer model is opt-in
    # because downloading/loading it during a request can exceed client timeouts.
    if os.getenv("USE_LOCAL_EMBEDDING_MODEL", "false").lower() != "true":
        return _local_embedding(text)
    model = _load_local_embedding_model()
    vector = model.encode(text, convert_to_numpy=True, normalize_embeddings=False)
    embedding = vector.tolist() if hasattr(vector, "tolist") else list(vector)
    return _normalize_embedding(embedding)