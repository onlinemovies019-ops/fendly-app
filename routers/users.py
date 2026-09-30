from typing import Any
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import User, UsernameReservation
from schemas import UsernameRequest


router = APIRouter(prefix="/api/users", tags=["users"])


class ProfileUpdate(BaseModel):
    username: str | None = None
    full_name: str | None = None
    first_name: str | None = None
    surname: str | None = None
    email: str | None = None
    mobile: str | None = None
    state: str | None = None
    city: str | None = None
    profile_photo_url: str | None = None
    email_verified: bool | None = None
    mobile_verified: bool | None = None

    class Config:
        extra = "ignore"


def normalize_username(value: str) -> str:
    base = value.strip().lower()
    if base.startswith("@"):
        base = base[1:]
    import re
    base = re.sub(r"[^a-z0-9_]", "_", base)
    base = re.sub(r"_+", "_", base)
    return base.strip("_")


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


@router.get("/profile")
def get_profile(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, Any]:
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None:
        user = User(firebase_uid=uid, email_verified=False, mobile_verified=False)
        session.add(user)
        try:
            session.commit()
            session.refresh(user)
        except Exception:
            session.rollback()

    first_name = getattr(user, "first_name", None) or ""
    surname = getattr(user, "surname", None) or ""
    full_name = getattr(user, "full_name", None) or ""

    if not first_name and full_name:
        parts = full_name.split(" ", 1)
        first_name = parts[0]
        surname = parts[1] if len(parts) > 1 else ""

    if not full_name and (first_name or surname):
        full_name = f"{first_name} {surname}".strip()

    return {
        "username": user.username or "",
        "full_name": full_name,
        "first_name": first_name,
        "surname": surname,
        "email": user.email or "",
        "mobile": user.mobile or "",
        "state": user.state or "",
        "city": user.city or "",
        "profile_photo_url": user.profile_photo_url or "",
        "email_verified": getattr(user, "email_verified", False),
        "mobile_verified": getattr(user, "mobile_verified", False),
        "is_verified": bool(getattr(user, "email_verified", False)),
    }


@router.put("/profile")
def update_profile(
    payload: ProfileUpdate,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None:
        user = User(firebase_uid=uid)
        session.add(user)

    if payload.username and payload.username.strip():
        username_value = normalize_username(payload.username)
        if username_value:
            reservation = session.get(UsernameReservation, username_value)
            if reservation is not None and reservation.firebase_uid != uid:
                raise HTTPException(409, "Username is already taken")
            user.username = username_value

    if payload.first_name is not None:
        user.first_name = payload.first_name.strip()
    if payload.surname is not None:
        user.surname = payload.surname.strip()

    if payload.full_name is not None and payload.full_name.strip():
        user.full_name = payload.full_name.strip()
    else:
        fn = getattr(user, "first_name", "") or ""
        sn = getattr(user, "surname", "") or ""
        if fn or sn:
            user.full_name = f"{fn} {sn}".strip()

    if payload.email is not None:
        user.email = payload.email.strip().lower()
    if payload.mobile is not None:
        user.mobile = payload.mobile.strip()
    if payload.state is not None:
        user.state = payload.state.strip()
    if payload.city is not None:
        user.city = payload.city.strip()
    if payload.profile_photo_url is not None:
        user.profile_photo_url = payload.profile_photo_url.strip()
    if payload.email_verified is not None:
        user.email_verified = payload.email_verified
    if payload.mobile_verified is not None:
        user.mobile_verified = payload.mobile_verified

    session.commit()
    return {"status": "saved"}