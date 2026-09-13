import logging

import firebase_admin
from firebase_admin import messaging
from sqlalchemy import select
from sqlalchemy.orm import Session

from models import DeviceToken


logger = logging.getLogger(__name__)


def send_match_notifications(
    session: Session,
    recipient_uids: set[str],
    found_item_id: str,
    score: float,
) -> int:
    if not recipient_uids:
        return 0

    tokens = session.scalars(
        select(DeviceToken.token).where(DeviceToken.firebase_uid.in_(recipient_uids))
    ).all()
    if not tokens:
        return 0

    try:
        firebase_admin.get_app()
        response = messaging.send_each_for_multicast(
            messaging.MulticastMessage(
                tokens=tokens,
                notification=messaging.Notification(
                    title="Possible Fendly match",
                    body=f"A found item matches yours ({score:.0%} match).",
                ),
                data={"found_item_id": found_item_id, "score": f"{score:.4f}"},
            )
        )
    except Exception:
        logger.exception("FCM notification delivery failed")
        return 0

    return response.success_count