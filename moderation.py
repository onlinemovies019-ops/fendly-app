import asyncio
import base64
import logging
import os
import re
from collections.abc import Sequence

import httpx
from fastapi import HTTPException, status
from storage3.utils import StorageException

from image_storage import get_private_image_for_moderation


logger = logging.getLogger(__name__)


def _moderation_error_details(response: httpx.Response) -> tuple[str, str]:
    try:
        error = response.json().get("error", {})
    except (ValueError, TypeError, AttributeError):
        return "", ""
    if not isinstance(error, dict):
        return "", ""
    code = error.get("code")
    message = error.get("message")
    safe_code = str(code)[:100] if code is not None else ""
    safe_message = str(message) if message is not None else ""
    safe_message = re.sub(r"https?://\S+", "[redacted URL]", safe_message)
    return safe_code, safe_message[:500]


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

    text = f"{title}\n{description}".strip()
    text_input = {"type": "text", "text": text} if text else None
    remote_image_urls: list[str] = []
    private_image_data: list[tuple[bytes, str]] = []
    try:
        for image_url in image_urls:
            private_image = await asyncio.to_thread(get_private_image_for_moderation, image_url)
            if private_image is None:
                remote_image_urls.append(image_url)
            else:
                private_image_data.append(private_image)
    except (StorageException, httpx.HTTPError, OSError, RuntimeError, ValueError) as exc:
        logger.exception("Unable to load private image for content moderation")
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "Content safety checks are temporarily unavailable; please try again",
        ) from exc
    image_inputs = [
        *(_moderation_image_input(image_url=url) for url in remote_image_urls),
        *(
            _moderation_image_input(image_data=data, image_content_type=content_type)
            for data, content_type in private_image_data
        ),
    ]
    if image_data is not None:
        image_inputs.append(_moderation_image_input(
            image_data=image_data,
            image_content_type=image_content_type,
        ))
    if not image_inputs and text_input is None:
        raise ValueError("Moderation requires text or at least one image")

    endpoint = os.getenv("OPENAI_MODERATION_URL", "https://api.openai.com/v1/moderations")
    has_images = bool(image_inputs)
    model = (
        "omni-moderation-latest"
        if has_images
        else os.getenv("OPENAI_MODERATION_MODEL", "omni-moderation-latest")
    )
    input_batches: list[list[dict[str, object]]] = []
    if image_inputs:
        for image_input in image_inputs:
            batch = [text_input] if text_input is not None else []
            batch.append(image_input)
            input_batches.append(batch)
    elif text_input is not None:
        input_batches.append([text_input])
    try:
        all_results: list[dict[str, object]] = []
        async with httpx.AsyncClient(timeout=15) as client:
            for input_batch in input_batches:
                response = await client.post(
                    endpoint,
                    json={"model": model, "input": input_batch},
                    headers={"Authorization": f"Bearer {api_key}"},
                )
                response.raise_for_status()
                batch_results = response.json().get("results")
                if not isinstance(batch_results, list) or not batch_results or any(
                    not isinstance(result, dict) or not isinstance(result.get("flagged"), bool)
                    for result in batch_results
                ):
                    raise ValueError("Moderation provider returned an invalid response")
                all_results.extend(batch_results)
        results = all_results
    except httpx.HTTPStatusError as exc:
        error_code, error_message = _moderation_error_details(exc.response)
        logger.error(
            "Content safety moderation rejected request: status=%s code=%s message=%s",
            exc.response.status_code,
            error_code,
            error_message,
        )
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "Content safety checks are temporarily unavailable; please try again",
        ) from exc
    except (httpx.HTTPError, ValueError, TypeError, AttributeError) as exc:
        logger.exception("Content safety moderation failed")
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "Content safety checks are temporarily unavailable; please try again",
        ) from exc

    if any(result["flagged"] for result in results):
        return "Content did not pass safety moderation"
    return None
