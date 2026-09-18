from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import User, UsernameReservation
from schemas import ProfileUpdate, UsernameRequest


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


@router.put("/profile")
def update_profile(
    payload: ProfileUpdate,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    normalized = normalize_username(payload.username)
    reservation = session.get(UsernameReservation, normalized)
    if reservation is not None and reservation.firebase_uid != uid:
        raise HTTPException(409, "Username is already taken")
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None:
        user = User(firebase_uid=uid)
        session.add(user)
    user.username = normalized
    user.full_name = payload.full_name.strip()
    user.email = payload.email.strip().lower()
    user.mobile = payload.mobile.strip()
    session.commit()
    return {"status": "saved"}
