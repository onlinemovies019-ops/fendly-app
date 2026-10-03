import re

from sqlalchemy import select
from sqlalchemy.orm import Session

from models import User
from schemas import ProfileUpdate


def update_profile_record(session: Session, payload: ProfileUpdate, uid: str) -> None:
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None:
        user = User(firebase_uid=uid)
        session.add(user)

    if payload.username and payload.username.strip():
        user.username = re.sub(r"_+", "_", re.sub(r"[^a-z0-9_]", "_", payload.username.strip().lower())).strip("_")
    if payload.full_name and payload.full_name.strip():
        user.full_name = payload.full_name.strip()
    if payload.email and payload.email.strip():
        email = payload.email.strip().lower()
        if email != (user.email or "").strip().lower():
            user.email = email
            user.email_verified = False
    if payload.mobile and payload.mobile.strip():
        user.mobile = payload.mobile.strip()
    if payload.state and payload.state.strip():
        user.state = payload.state.strip()
    if payload.city and payload.city.strip():
        user.city = payload.city.strip()
    if payload.profile_photo_url and payload.profile_photo_url.strip():
        user.profile_photo_url = payload.profile_photo_url.strip()
    session.commit()
