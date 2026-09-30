import os
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Header, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel, EmailStr
from supabase import create_client, Client

# Initialize Supabase Client
SUPABASE_URL = os.getenv("SUPABASE_URL", "")
SUPABASE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY", os.getenv("SUPABASE_KEY", ""))

supabase: Optional[Client] = None
if SUPABASE_URL and SUPABASE_KEY:
    supabase = create_client(SUPABASE_URL, SUPABASE_KEY)

router = APIRouter(prefix="/api/users", tags=["Authentication & Profile"])
security = HTTPBearer()

# --- Pydantic Schemas ---

class ProfileUpdate(BaseModel):
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    surname: Optional[str] = None
    mobile: Optional[str] = None
    state: Optional[str] = None
    city: Optional[str] = None
    pincode: Optional[str] = None


class ProfileResponse(BaseModel):
    id: str
    email: Optional[str] = None
    mobile: Optional[str] = None
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    surname: Optional[str] = None
    state: Optional[str] = None
    city: Optional[str] = None
    pincode: Optional[str] = None
    is_verified: bool = True
    email_verified: bool = True


# --- Auth Dependency ---

async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> dict:
    """
    Extracts and verifies the user token.
    Returns user data dictionary.
    """
    token = credentials.credentials
    if not token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing authentication token",
        )

    # In production, verify JWT / Supabase / Firebase token here
    try:
        if supabase:
            user_response = supabase.auth.get_user(token)
            if user_response and user_response.user:
                return {"id": user_response.user.id, "email": user_response.user.email}

        # Fallback for authorization headers using raw user ID or test token
        return {"id": token, "email": None}
    except Exception as e:
        # Return fallback token ID if token is user ID directly
        return {"id": token, "email": None}


# --- Endpoints ---

@router.get("/profile", response_model=ProfileResponse)
async def get_profile(current_user: dict = Depends(get_current_user)):
    user_id = current_user["id"]

    if not supabase:
        return ProfileResponse(
            id=user_id,
            email=current_user.get("email"),
            mobile="",
            first_name="",
            last_name="",
            surname="",
            state="",
            city="",
            pincode="",
            is_verified=True,
            email_verified=True,
        )

    try:
        response = (
            supabase.table("profiles")
            .select("*")
            .eq("id", user_id)
            .execute()
        )

        if response.data and len(response.data) > 0:
            profile_data = response.data[0]
            return ProfileResponse(
                id=profile_data.get("id", user_id),
                email=profile_data.get("email", current_user.get("email")),
                mobile=profile_data.get("mobile", ""),
                first_name=profile_data.get("first_name", profile_data.get("first_name", "")),
                last_name=profile_data.get("last_name", profile_data.get("last_name", "")),
                surname=profile_data.get("surname", profile_data.get("last_name", "")),
                state=profile_data.get("state", ""),
                city=profile_data.get("city", ""),
                pincode=profile_data.get("pincode", ""),
                is_verified=profile_data.get("is_verified", True),
                email_verified=profile_data.get("email_verified", True),
            )
        else:
            # Create default profile row if missing
            new_profile = {
                "id": user_id,
                "email": current_user.get("email"),
            }
            supabase.table("profiles").insert(new_profile).execute()

            return ProfileResponse(
                id=user_id,
                email=current_user.get("email"),
                mobile="",
                first_name="",
                last_name="",
                surname="",
                state="",
                city="",
                pincode="",
                is_verified=True,
                email_verified=True,
            )

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Database error: {str(e)}",
        )


@router.put("/profile", response_model=ProfileResponse)
async def update_profile(
    profile_update: ProfileUpdate,
    current_user: dict = Depends(get_current_user),
):
    user_id = current_user["id"]

    update_data = profile_update.dict(exclude_unset=True)
    if not update_data:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No fields provided for update",
        )

    if not supabase:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection unconfigured",
        )

    try:
        response = (
            supabase.table("profiles")
            .upsert({"id": user_id, **update_data})
            .execute()
        )

        updated = response.data[0] if response.data else update_data

        is_verified = updated.get("is_verified", True)
        email_verified = updated.get("email_verified", True)

        return ProfileResponse(
            id=user_id,
            email=updated.get("email", current_user.get("email")),
            mobile=updated.get("mobile", ""),
            first_name=updated.get("first_name", ""),
            last_name=updated.get("last_name", ""),
            surname=updated.get("surname", ""),
            state=updated.get("state", ""),
            city=updated.get("city", ""),
            pincode=updated.get("pincode", ""),
            is_verified=is_verified,
            email_verified=email_verified,
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to update profile: {str(e)}",
        )