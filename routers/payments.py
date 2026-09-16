import os

import razorpay
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field

from auth import get_current_user


router = APIRouter(prefix="/api/payments", tags=["payments"])
PAYMENT_AMOUNT_PAISE = 9900
PAYMENT_CURRENCY = "INR"


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
    uid: str = Depends(get_current_user),
) -> dict[str, str | bool]:
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

    return {"verified": True, "payment_id": payload.razorpay_payment_id}


def verify_captured_payment(payment_id: str, uid: str) -> None:
    client = _client()
    try:
        payment = client.payment.fetch(payment_id)
        order = client.order.fetch(payment["order_id"])
    except Exception as exc:
        raise HTTPException(status.HTTP_402_PAYMENT_REQUIRED, "A valid payment is required") from exc
    notes = order.get("notes") or {}
    if (
        notes.get("firebase_uid") != uid
        or payment.get("amount") != PAYMENT_AMOUNT_PAISE
        or payment.get("currency") != PAYMENT_CURRENCY
        or payment.get("status") != "captured"
    ):
        raise HTTPException(status.HTTP_402_PAYMENT_REQUIRED, "A valid payment is required")
