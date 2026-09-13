from fastapi import APIRouter, Depends, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import DeviceToken
from schemas import FcmTokenRequest


router = APIRouter(prefix="/api/devices", tags=["notifications"])


@router.post("/fcm-token", status_code=status.HTTP_204_NO_CONTENT)
def register_fcm_token(
    payload: FcmTokenRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> None:
    device = session.scalar(select(DeviceToken).where(DeviceToken.token == payload.token))
    if device is None:
        device = DeviceToken(firebase_uid=uid, token=payload.token, platform=payload.platform)
        session.add(device)
    else:
        device.firebase_uid = uid
        device.platform = payload.platform
    session.commit()


@router.delete("/fcm-token", status_code=status.HTTP_204_NO_CONTENT)
def remove_fcm_token(
    payload: FcmTokenRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> None:
    device = session.scalar(
        select(DeviceToken).where(DeviceToken.token == payload.token, DeviceToken.firebase_uid == uid)
    )
    if device is not None:
        session.delete(device)
        session.commit()