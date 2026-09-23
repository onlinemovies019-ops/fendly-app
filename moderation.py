import os
import re

import httpx


LOCAL_BLOCKLIST = {
    "scam",
    "fraud",
    "weapon",
    "drug",
    "bomb",
    "explicit",
}
WORD_PATTERN = re.compile(r"[a-z0-9]+")


async def moderate_content(title: str, description: str) -> str | None:
    text = f"{title}\n{description}"
    api_key = os.getenv("OPENAI_API_KEY")
    if api_key:
        try:
            endpoint = os.getenv("OPENAI_MODERATION_URL", "https://api.openai.com/v1/moderations")
            async with httpx.AsyncClient(timeout=20) as client:
                response = await client.post(
                    endpoint,
                    json={"model": os.getenv("OPENAI_MODERATION_MODEL", "omni-moderation-latest"), "input": text},
                    headers={"Authorization": f"Bearer {api_key}"},
                )
            response.raise_for_status()
            result = response.json().get("results", [{}])[0]
            if result.get("flagged"):
                return "Content did not pass safety moderation"
            return None
        except (httpx.HTTPError, ValueError, KeyError):
            pass

    words = set(WORD_PATTERN.findall(text.lower()))
    blocked = sorted(words.intersection(LOCAL_BLOCKLIST))
    return f"Content contains blocked terms: {', '.join(blocked)}" if blocked else None
