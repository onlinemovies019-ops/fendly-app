import os

import httpx


EMBEDDING_DIMENSIONS = 1536
DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small"


def item_text(title: str, description: str, category: str) -> str:
    return f"Category: {category}\nTitle: {title}\nDescription: {description}"


async def create_embedding(text: str) -> list[float] | None:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        return None
    endpoint = os.getenv("OPENAI_EMBEDDINGS_URL", "https://api.openai.com/v1/embeddings")
    payload = {
        "input": text,
        "model": os.getenv("OPENAI_EMBEDDING_MODEL", DEFAULT_EMBEDDING_MODEL),
        "dimensions": EMBEDDING_DIMENSIONS,
    }
    async with httpx.AsyncClient(timeout=30) as client:
        response = await client.post(endpoint, json=payload, headers={"Authorization": f"Bearer {api_key}"})
    response.raise_for_status()
    embedding = response.json()["data"][0]["embedding"]
    if len(embedding) != EMBEDDING_DIMENSIONS:
        raise ValueError("Embedding provider returned an unexpected vector size")
    return embedding