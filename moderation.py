import base64
import logging
import os
from collections.abc import Sequence

import httpx
from fastapi import HTTPException, status


logger = logging.getLogger(__name__)


def _moderation_image_input(
    *,
    image_url: str | None = None,
    image_data: bytes | None = None,
    image_content_type: str | None = None,
) -> dict[str, object]:
    if image_data is not None:
        if image_content_type not in {"image/jpeg", "image/png", "image/webp"}:
            raise HTTPException(status.HTTP_415_UNSUPPORTED_MEDIA_TYPE, "Unsupported image type")
        encoded = base64.b64encode(image_data).decode("ascii")
        url = f"data:{image_content_type};base64,{encoded}"
    elif image_url:
        url = image_url
    else:
        raise ValueError("An image URL or image body is required")
    return {"type": "image_url", "image_url": {"url": url}}


async def moderate_content(
    title: str,
    description: str,
    *,
    image_urls: Sequence[str] = (),
    image_data: bytes | None = None,
    image_content_type: str | None = None,
) -> str | None:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "Content safety checks are temporarily unavailable",
        )

    inputs: list[dict[str, object]] = []
    text = f"{title}\n{description}".strip()
    if text:
        inputs.append({"type": "text", "text": text})
    inputs.extend(_moderation_image_input(image_url=url) for url in image_urls)
    if image_data is not None:
        inputs.append(_moderation_image_input(
            image_data=image_data,
            image_content_type=image_content_type,
        ))
    if not inputs:
        raise ValueError("Moderation requires text or at least one image")

    endpoint = os.getenv("OPENAI_MODERATION_URL", "https://api.openai.com/v1/moderations")
    model = os.getenv("OPENAI_MODERATION_MODEL", "omni-moderation-latest")
    try:
        async with httpx.AsyncClient(timeout=15) as client:
            response = await client.post(
                endpoint,
                json={"model": model, "input": inputs},
                headers={"Authorization": f"Bearer {api_key}"},
            )
        response.raise_for_status()
        results = response.json().get("results")
        if not isinstance(results, list) or not results or any(
            not isinstance(result, dict) or not isinstance(result.get("flagged"), bool)
            for result in results
        ):
            raise ValueError("Moderation provider returned an invalid response")
    except (httpx.HTTPError, ValueError, TypeError, AttributeError) as exc:
        logger.exception("Content safety moderation failed")
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "Content safety checks are temporarily unavailable; please try again",
        ) from exc

    if any(result["flagged"] for result in results):
        return "Content did not pass safety moderation"
    return None
