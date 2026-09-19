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
- `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, and `SUPABASE_STORAGE_BUCKET`:
	configure these to store uploads permanently in a public Supabase Storage
	bucket. The service-role key must remain server-side only.
- Matching is local and free: keyword similarity is combined with location
	proximity. OpenAI is optional; if absent, the app falls back to a local
	free sentence-transformers model.

Uploads use Supabase Storage when configured. Without those variables, local
development writes to `static/uploads`; Render's free filesystem is ephemeral.

## API

- `GET /health`
- `POST /api/upload`
- `POST /api/items/lost`
- `POST /api/items/found`
- `GET /api/items/mine`
- `POST /api/items/match`
- `GET /api/users/username/{username}`
- `POST /api/users/username`
- `GET /api/admin/items`
- `GET /api/admin/matches/{found_item_id}`
- `POST /api/admin/matches/{found_item_id}/notify`
- `POST /api/devices/fcm-token`
- `DELETE /api/devices/fcm-token`

All `/api` endpoints require `Authorization: Bearer <Firebase ID token>`.
Admin endpoints additionally require the Firebase UID to be listed in
`ADMIN_FIREBASE_UIDS`. Set `OPENAI_API_KEY` to enable provider-backed moderation;
without it, the backend uses its local safety blocklist.
