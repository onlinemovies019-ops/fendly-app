from fastapi import APIRouter, Depends, status
from sqlalchemy import desc, func, select, update
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import DeviceToken, UserNotification
from schemas import FcmTokenRequest


router = APIRouter(prefix="/api/devices", tags=["notifications"])
inbox_router = APIRouter(prefix="/api/notifications", tags=["notifications"])


def get_user_notification_payload(session: Session, uid: str) -> dict[str, object]:
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
                "type": notification.notification_type,
                "match_alert_id": notification.match_alert_id,
                "found_item_id": notification.found_item_id,
                "lost_item_id": notification.lost_item_id,
                "title": notification.title,
                "body": notification.body,
                "score": notification.score,
                "is_read": notification.is_read,
                "created_at": notification.created_at.isoformat()
                if notification.created_at
                else None,
            }
            for notification in notifications
        ],
    }


def mark_user_notifications_read(session: Session, uid: str) -> int:
    result = session.execute(
        update(UserNotification)
        .where(
            UserNotification.firebase_uid == uid,
            UserNotification.is_read.is_(False),
        )
        .values(is_read=True)
    )
    session.commit()
    return result.rowcount or 0


@inbox_router.get("")
def list_notifications(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, object]:
    return get_user_notification_payload(session, uid)


@inbox_router.post("/read")
def mark_notifications_read(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, int]:
    return {"updated": mark_user_notifications_read(session, uid)}


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