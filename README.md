# Fendly API

FastAPI backend for the Fendly Android lost-and-found app. Firebase Auth verifies
Bearer ID tokens, Supabase PostgreSQL stores item records, and Render runs the
service.

## Local development

```bash
python3.11 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload
```

Required environment variables:

- `DATABASE_URL`: Supabase PostgreSQL connection string. The `vector` extension
	must be enabled in the Supabase project.
- `FIREBASE_SERVICE_ACCOUNT_JSON`, or `GOOGLE_APPLICATION_CREDENTIALS`: Firebase
	Admin credentials used to verify Android ID tokens.
- `PUBLIC_BASE_URL`: Public API URL used in uploaded image URLs.
- `CORS_ORIGINS`: Comma-separated allowed origins. Defaults to `*`.

Uploads are written to `static/uploads` by default. Render's free filesystem is
ephemeral, so configure `UPLOAD_DIR` or replace the upload implementation with a
Supabase Storage bucket before relying on uploads in production.

## API

- `GET /health`
- `POST /api/upload`
- `POST /api/items/lost`
- `POST /api/items/found`
- `POST /api/items/match`

All `/api` endpoints require `Authorization: Bearer <Firebase ID token>`.
