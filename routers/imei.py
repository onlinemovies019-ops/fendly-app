from fastapi import APIRouter, HTTPException, Response, status

router = APIRouter(prefix="/api/v1/imei", tags=["SafeTrade"])

@router.get("/verify/{imei_number}")
def verify_imei(
    imei_number: str,
    response: Response,
) -> None:
    response.headers["Cache-Control"] = "no-store"
    raise HTTPException(
        status_code=status.HTTP_410_GONE,
        detail="SafeTrade IMEI verification has been disabled",
    )
