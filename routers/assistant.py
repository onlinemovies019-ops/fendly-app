import logging

import httpx
from fastapi import APIRouter, Depends, HTTPException, status

from auth import get_current_user
from fendly_assistant import (
    AssistantChatRequest,
    AssistantChatResponse,
    generate_fendly_answer,
)


router = APIRouter(prefix="/api/assistant", tags=["Fendly Help"])
logger = logging.getLogger(__name__)


@router.post("/chat", response_model=AssistantChatResponse)
async def chat_with_fendly_assistant(
    payload: AssistantChatRequest,
    _: str = Depends(get_current_user),
) -> AssistantChatResponse:
    try:
        answer = await generate_fendly_answer(payload)
    except RuntimeError as error:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=str(error),
        ) from error
    except httpx.HTTPError as error:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="Fendly Help could not reach Gemini. Please try again.",
        ) from error
    except (ValueError, KeyError, IndexError, TypeError) as error:
        logger.exception("Gemini returned an invalid Fendly Help response")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="Fendly Help received an invalid response. Please try again.",
        ) from error
    return AssistantChatResponse(answer=answer)
