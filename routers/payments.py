import os
import time

import razorpay
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
from sqlalchemy import select
from sqlalchemy.orm import Session

from auth import get_current_user
from database import get_db
from models import User


router = APIRouter(prefix="/api/payments", tags=["payments"])
PAYMENT_AMOUNT_PAISE = 9900
PAYMENT_CURRENCY = "INR"
SUBSCRIPTION_DURATION_MS = 365 * 24 * 60 * 60 * 1000


def _client() -> razorpay.Client:
    key_id = os.getenv("RAZORPAY_KEY_ID")
    key_secret = os.getenv("RAZORPAY_KEY_SECRET")
    if not key_id or not key_secret:
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "Payment gateway is not configured")
    return razorpay.Client(auth=(key_id, key_secret))


class PaymentOrderResponse(BaseModel):
    order_id: str
    key_id: str
    amount: int
    currency: str


class PaymentVerificationRequest(BaseModel):
    razorpay_order_id: str = Field(min_length=1, max_length=128)
    razorpay_payment_id: str = Field(min_length=1, max_length=128)
    razorpay_signature: str = Field(min_length=1, max_length=256)


@router.post("/orders", response_model=PaymentOrderResponse)
def create_payment_order(uid: str = Depends(get_current_user)) -> PaymentOrderResponse:
    client = _client()
    order = client.order.create({
        "amount": PAYMENT_AMOUNT_PAISE,
        "currency": PAYMENT_CURRENCY,
        "receipt": f"fendly_{uid[:24]}",
        "notes": {"firebase_uid": uid, "purpose": "lost_report"},
    })
    return PaymentOrderResponse(
        order_id=order["id"],
        key_id=os.environ["RAZORPAY_KEY_ID"],
        amount=PAYMENT_AMOUNT_PAISE,
        currency=PAYMENT_CURRENCY,
    )


@router.post("/verify")
def verify_payment(
    payload: PaymentVerificationRequest,
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str | bool | int]:
    client = _client()
    try:
        client.utility.verify_payment_signature({
            "razorpay_order_id": payload.razorpay_order_id,
            "razorpay_payment_id": payload.razorpay_payment_id,
            "razorpay_signature": payload.razorpay_signature,
        })
        payment = client.payment.fetch(payload.razorpay_payment_id)
        order = client.order.fetch(payload.razorpay_order_id)
    except Exception as exc:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Payment verification failed") from exc

    notes = order.get("notes") or {}
    if notes.get("firebase_uid") != uid:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Payment does not belong to this account")
    if payment.get("order_id") != payload.razorpay_order_id:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Payment order mismatch")
    if payment.get("amount") != PAYMENT_AMOUNT_PAISE or payment.get("currency") != PAYMENT_CURRENCY:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Payment amount mismatch")
    if payment.get("status") != "captured":
        raise HTTPException(status.HTTP_402_PAYMENT_REQUIRED, "Payment is not captured")

    user = _grant_annual_subscription(session, uid, payload.razorpay_payment_id)
    return {
        "verified": True,
        "payment_id": payload.razorpay_payment_id,
        "expires_at": user.annual_subscription_expires_at,
    }


def _grant_annual_subscription(session: Session, uid: str, payment_id: str) -> User:
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None:
        user = User(firebase_uid=uid, email_verified=False, mobile_verified=False)
        session.add(user)

    if user.annual_subscription_payment_id != payment_id:
        user.annual_subscription_expires_at = int(time.time() * 1000) + SUBSCRIPTION_DURATION_MS
        user.annual_subscription_payment_id = payment_id
        session.commit()
        session.refresh(user)
    return user


def _recover_legacy_subscription(session: Session, uid: str) -> User | None:
    client = _client()
    receipt = f"fendly_{uid[:24]}"
    try:
        orders = client.order.all({"receipt": receipt, "count": 100}).get("items", [])
        latest_payment = None
        for order in orders:
            notes = order.get("notes") or {}
            if notes.get("firebase_uid") != uid or notes.get("purpose") != "lost_report":
                continue
            payments = client.order.payments(order["id"]).get("items", [])
            for payment in payments:
                if (
                    payment.get("status") == "captured"
                    and payment.get("amount") == PAYMENT_AMOUNT_PAISE
                    and payment.get("currency") == PAYMENT_CURRENCY
                    and (latest_payment is None or payment.get("created_at", 0) > latest_payment.get("created_at", 0))
                ):
                    latest_payment = payment
    except Exception as exc:
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "Subscription status is unavailable") from exc

    if latest_payment is None:
        return None

    expires_at = int(latest_payment.get("created_at", 0)) * 1000 + SUBSCRIPTION_DURATION_MS
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None:
        user = User(firebase_uid=uid, email_verified=False, mobile_verified=False)
        session.add(user)
    user.annual_subscription_expires_at = expires_at
    user.annual_subscription_payment_id = latest_payment["id"]
    session.commit()
    session.refresh(user)
    return user


@router.get("/subscription")
def get_subscription(
    session: Session = Depends(get_db),
    uid: str = Depends(get_current_user),
) -> dict[str, str | bool | int | None]:
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if user is None or user.annual_subscription_expires_at is None:
        user = _recover_legacy_subscription(session, uid)

    expires_at = user.annual_subscription_expires_at if user is not None else None
    active = expires_at is not None and expires_at > int(time.time() * 1000)
    return {
        "active": active,
        "expires_at": expires_at,
        "payment_id": user.annual_subscription_payment_id if active and user is not None else None,
    }


def verify_captured_payment(payment_id: str, uid: str) -> None:
    client = _client()
    try:
        payment = client.payment.fetch(payment_id)
        order = client.order.fetch(payment["order_id"])
    except Exception as exc:
        raise HTTPException(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "Payment verification is temporarily unavailable. Please try again.",
        ) from exc
    notes = order.get("notes") or {}
    if (
        notes.get("firebase_uid") != uid
        or payment.get("amount") != PAYMENT_AMOUNT_PAISE
        or payment.get("currency") != PAYMENT_CURRENCY
        or payment.get("status") != "captured"
    ):
        raise HTTPException(status.HTTP_402_PAYMENT_REQUIRED, "A valid payment is required")


def require_lost_report_entitlement(
    session: Session,
    uid: str,
    payment_id: str | None,
) -> None:
    user = session.scalar(select(User).where(User.firebase_uid == uid))
    if (
        user is not None
        and user.annual_subscription_expires_at is not None
        and user.annual_subscription_expires_at > int(time.time() * 1000)
    ):
        return
    if not payment_id:
        raise HTTPException(status.HTTP_402_PAYMENT_REQUIRED, "A valid payment is required")
    verify_captured_payment(payment_id, uid)
