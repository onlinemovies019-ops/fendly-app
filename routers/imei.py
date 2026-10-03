import logging
import time

from fastapi import APIRouter, Depends, HTTPException, Request, Response, status
from pydantic import BaseModel
from sqlalchemy import case, select
from sqlalchemy.dialects.postgresql import insert as postgresql_insert
from sqlalchemy.dialects.sqlite import insert as sqlite_insert
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session

from database import get_db
from imei_security import client_quota_digest, imei_digest, validate_imei
from models import LostItem, PublicImeiLookupRateLimit

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/imei", tags=["SafeTrade"])

RATE_LIMIT_REQUESTS = 10
RATE_LIMIT_WINDOW_SECONDS = 60


class ImeiVerificationResponse(BaseModel):
    status: str
    message: str
    is_flagged: bool


def _reserve_public_lookup(request: Request, session: Session) -> None:
    client_ip = request.client.host if request.client else None
    if not client_ip:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="IMEI verification is temporarily unavailable",
        )

    now = int(time.time())
    quota_key = client_quota_digest(client_ip)
    dialect = session.get_bind().dialect.name
    insert = (
        postgresql_insert(PublicImeiLookupRateLimit)
        if dialect == "postgresql"
        else sqlite_insert(PublicImeiLookupRateLimit)
        if dialect == "sqlite"
        else None
    )
    if insert is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="IMEI verification is temporarily unavailable",
        )

    expired_window = PublicImeiLookupRateLimit.window_started <= now - RATE_LIMIT_WINDOW_SECONDS
    statement = insert.values(
        quota_key=quota_key,
        window_started=now,
        request_count=1,
    ).on_conflict_do_update(
        index_elements=[PublicImeiLookupRateLimit.quota_key],
        set_={
            "window_started": case(
                (expired_window, now),
                else_=PublicImeiLookupRateLimit.window_started,
            ),
            "request_count": case(
                (expired_window, 1),
                else_=PublicImeiLookupRateLimit.request_count + 1,
            ),
        },
    ).returning(
        PublicImeiLookupRateLimit.window_started,
        PublicImeiLookupRateLimit.request_count,
    )

    try:
        window_started, request_count = session.execute(statement).one()
        session.commit()
    except SQLAlchemyError as exc:
        session.rollback()
        logger.exception("SafeTrade rate-limit reservation failed")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="IMEI verification is temporarily unavailable",
        ) from exc

    if request_count > RATE_LIMIT_REQUESTS:
        retry_after = max(1, RATE_LIMIT_WINDOW_SECONDS - (now - window_started))
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Too many IMEI checks. Please try again shortly.",
            headers={"Retry-After": str(retry_after)},
        )


@router.get("/verify/{imei_number}", response_model=ImeiVerificationResponse)
def verify_imei(
    imei_number: str,
    request: Request,
    response: Response,
    session: Session = Depends(get_db),
) -> ImeiVerificationResponse:
    validate_imei(imei_number)
    response.headers["Cache-Control"] = "no-store"
    _reserve_public_lookup(request, session)
    digest = imei_digest(imei_number)

    try:
        is_flagged = session.scalar(
            select(LostItem.id)
            .where(
                LostItem.status == "LOST",
                LostItem.imei_hash == digest,
            )
            .limit(1)
        ) is not None
    except SQLAlchemyError as exc:
        session.rollback()
        logger.exception("SafeTrade report lookup failed")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="IMEI verification is temporarily unavailable",
        ) from exc

    return ImeiVerificationResponse(
        status="FLAGGED" if is_flagged else "CLEAN",
        message=(
            "This device is currently reported missing. Do not complete purchase."
            if is_flagged
            else "No active loss reports were found for this device."
        ),
        is_flagged=is_flagged,
    )
