from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import UsernameReservation
from schemas import UsernameRequest


router = APIRouter(prefix="/api/users", tags=["users"])


def normalize_username(value: str) -> str:
    return value.strip().lower()


@router.get("/username/{username}")
def check_username(
    username: str,
    session: Session = Depends(get_db),
    _: str = Depends(get_current_user),
) -> dict[str, object]:
    normalized = normalize_username(username)
    if len(normalized) < 3 or len(normalized) > 32 or not normalized.replace("_", "").isalnum():
        return {"username": normalized, "available": False}
    taken = session.get(UsernameReservation, normalized) is not None
    return {"username": normalized, "available": not taken}


@router.post("/username", status_code=201)
def reserve_username(
    payload: UsernameRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    normalized = normalize_username(payload.username)
    existing = session.get(UsernameReservation, normalized)
    if existing is not None and existing.firebase_uid != uid:
        raise HTTPException(409, "Username is already taken")
    owned = session.scalar(select(UsernameReservation).where(UsernameReservation.firebase_uid == uid))
    if owned is not None and owned.username != normalized:
        session.delete(owned)
        session.flush()
    if existing is None:
        session.add(UsernameReservation(username=normalized, firebase_uid=uid))
        try:
            session.commit()
        except IntegrityError as exc:
            session.rollback()
            raise HTTPException(409, "Username is already taken") from exc
    return {"username": normalized, "status": "reserved"}
