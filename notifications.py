import logging
import os
import requests
from html import escape

import firebase_admin
from firebase_admin import messaging
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from models import AdminMatchAlert, DeviceToken, FoundItem, LostItem


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


def send_admin_match_email(item_title: str, match_score: float, match_details: str):
    api_key = os.getenv("BREVO_API_KEY")
    admin_email = os.getenv("ADMIN_EMAIL")
    sender_email = os.getenv("SENDER_EMAIL")

    if not api_key or not admin_email or not sender_email:
        logger.warning("Brevo email configuration missing (BREVO_API_KEY, ADMIN_EMAIL, SENDER_EMAIL)")
        return False

    url = "https://api.brevo.com/v3/smtp/email"
    headers = {
        "api-key": api_key,
        "content-type": "application/json"
    }

    percentage = match_score * 100 if match_score <= 1.0 else match_score
    safe_title = escape(item_title)
    safe_details = escape(match_details)
    subject = f"Fendly match alert: {item_title}"
    html_content = f"""
    <html>
      <body>
        <h2>Fendly Match Alert</h2>
        <p><strong>Item Title:</strong> {safe_title}</p>
        <p><strong>Match Confidence:</strong> {percentage:.1f}%</p>
        <p><strong>Match Details:</strong> {safe_details}</p>
        <hr>
        <p><small>Fendly Automated Notification System</small></p>
      </body>
    </html>
    """

    payload = {
        "sender": {"name": "Fendly Notification Bot", "email": sender_email},
        "to": [{"email": admin_email}],
        "subject": subject,
        "htmlContent": html_content
    }

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=10)
        response.raise_for_status()
        logger.info("Admin match email sent successfully via Brevo")
        return True
    except Exception:
        logger.exception("Failed to send admin match email via Brevo")
        return False


def persist_admin_match_alert(
    session: Session,
    found_item: FoundItem,
    lost_item: LostItem,
    confidence: float,
) -> bool:
    existing = session.scalar(
        select(AdminMatchAlert).where(
            AdminMatchAlert.found_item_id == found_item.id,
            AdminMatchAlert.lost_item_id == lost_item.id,
        )
    )
    if existing is not None:
        return False

    details = f"Found '{found_item.title}' may match lost report '{lost_item.title}'."
    alert = AdminMatchAlert(
        found_item_id=found_item.id,
        lost_item_id=lost_item.id,
        found_title=found_item.title,
        lost_title=lost_item.title,
        confidence=confidence,
        reason=details,
    )
    session.add(alert)
    try:
        session.commit()
    except IntegrityError:
        session.rollback()
        return False
    except Exception:
        session.rollback()
        logger.exception("Failed to persist admin match alert")
        return False

    alert.email_sent = send_admin_match_email(
        found_item.title,
        confidence,
        f"{details} Confidence: {confidence:.1%}",
    )
    try:
        session.commit()
    except Exception:
        session.rollback()
        logger.exception("Failed to update admin match alert email status")
    return True