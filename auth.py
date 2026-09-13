import json
import os

import firebase_admin
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from firebase_admin import auth as firebase_auth
from firebase_admin import credentials


bearer_scheme = HTTPBearer(auto_error=False)


def _firebase_app() -> firebase_admin.App:
    if firebase_admin._apps:
        return firebase_admin.get_app()
    raw_credentials = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
    if raw_credentials:
        return firebase_admin.initialize_app(credentials.Certificate(json.loads(raw_credentials)))
    credentials_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    if credentials_path:
        return firebase_admin.initialize_app(credentials.Certificate(credentials_path))
    return firebase_admin.initialize_app(credentials.ApplicationDefault())


def get_current_user(
    token: HTTPAuthorizationCredentials | None = Depends(bearer_scheme),
) -> str:
    if token is None or token.scheme.lower() != "bearer":
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Bearer token required")
    try:
        _firebase_app()
        decoded_token = firebase_auth.verify_id_token(token.credentials)
        return decoded_token["uid"]
    except Exception as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid Firebase token") from exc