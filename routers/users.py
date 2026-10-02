import logging
from typing import Any
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import desc, func, select, update
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import User, UserNotification, UsernameReservation
from schemas import ProfileUpdate, UsernameRequest

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/users", tags=["users"])


@router.get("/notifications")
def get_user_notifications(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, object]:
    unread_count = session.scalar(
        select(func.count(UserNotification.id)).where(
            UserNotification.firebase_uid == uid,
            UserNotification.is_read.is_(False),
        )
    ) or 0
    notifications = session.scalars(
        select(UserNotification)
        .where(UserNotification.firebase_uid == uid)
        .order_by(desc(UserNotification.created_at))
        .limit(50)
    ).all()
    return {
        "unread_count": unread_count,
        "notifications": [
            {
                "id": notification.id,
                "found_item_id": notification.found_item_id,
                "title": notification.title,
                "body": notification.body,
                "score": notification.score,
                "is_read": notification.is_read,
                "created_at": notification.created_at.isoformat() if notification.created_at else None,
            }
            for notification in notifications
        ],
    }


@router.post("/notifications/read")
def mark_user_notifications_read(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, int]:
    result = session.execute(
        update(UserNotification)
        .where(
            UserNotification.firebase_uid == uid,
            UserNotification.is_read.is_(False),
        )
        .values(is_read=True)
    )
    session.commit()
    return {"updated": result.rowcount or 0}


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
    try:
        taken = session.get(UsernameReservation, normalized) is not None
    except Exception:
        taken = False
    return {"username": normalized, "available": not taken}


@router.post("/username", status_code=201)
def reserve_username(
    payload: UsernameRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    normalized = normalize_username(payload.username)
    try:
        existing = session.get(UsernameReservation, normalized)
        if existing is not None and existing.firebase_uid != uid:
            raise HTTPException(409, "Username is already taken")
        owned = session.scalar(select(UsernameReservation).where(UsernameReservation.firebase_uid == uid))
        if owned is not None and owned.username != normalized:
            session.delete(owned)
            session.flush()
        if existing is None:
            session.add(UsernameReservation(username=normalized, firebase_uid=uid))
            session.commit()
    except HTTPException:
        raise
    except Exception as exc:
        session.rollback()
        raise HTTPException(409, "Username is already taken") from exc
    return {"username": normalized, "status": "reserved"}


@router.get("/profile")
def get_profile(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, Any]:
    try:
        user = session.scalar(select(User).where(User.firebase_uid == uid))
        if user is None:
            user = User(firebase_uid=uid, email_verified=False, mobile_verified=False)
            session.add(user)
            try:
                session.commit()
                session.refresh(user)
            except Exception:
                session.rollback()
        return {
            "username": getattr(user, "username", None) or "",
            "full_name": getattr(user, "full_name", None) or "",
            "email": getattr(user, "email", None) or "",
            "mobile": getattr(user, "mobile", None) or "",
            "state": getattr(user, "state", None) or "",
            "city": getattr(user, "city", None) or "",
            "profile_photo_url": getattr(user, "profile_photo_url", None) or "",
            "email_verified": bool(getattr(user, "email_verified", False)),
            "mobile_verified": bool(getattr(user, "mobile_verified", False)),
            "is_verified": bool(getattr(user, "email_verified", False)),
        }
    except Exception as exc:
        logger.error("Profile fetch error: %s", exc)
        return {
            "username": "",
            "full_name": "",
            "email": "",
            "mobile": "",
            "state": "",
            "city": "",
            "profile_photo_url": "",
            "email_verified": False,
            "mobile_verified": False,
            "is_verified": False,
        }


@router.put("/profile")
def update_profile(
    payload: ProfileUpdate,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str]:
    try:
        user = session.scalar(select(User).where(User.firebase_uid == uid))
        if user is None:
            user = User(firebase_uid=uid)
            session.add(user)

        if payload.username and payload.username.strip():
            username_value = normalize_username(payload.username)
            user.username = username_value

        if payload.full_name is not None:
            user.full_name = payload.full_name.strip()
        if payload.email is not None:
            user.email = payload.email.strip().lower()
        if payload.mobile is not None:
            user.mobile = payload.mobile.strip()
        if payload.state is not None:
            user.state = payload.state.strip()
        if payload.city is not None:
            user.city = payload.city.strip()
        if hasattr(payload, "profile_photo_url") and payload.profile_photo_url is not None:
            user.profile_photo_url = payload.profile_photo_url.strip()
        if payload.email_verified is not None:
            user.email_verified = payload.email_verified
        if payload.mobile_verified is not None:
            user.mobile_verified = payload.mobile_verified

        session.commit()
        return {"status": "saved"}
    except HTTPException:
        raise
    except Exception as exc:
        session.rollback()
        logger.error("Profile update error: %s", exc)
        raise HTTPException(500, f"Failed to update profile: {exc}")
