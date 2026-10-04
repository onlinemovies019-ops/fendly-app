# Fendly API

FastAPI backend for the Fendly Android lost-and-found app. Firebase Auth verifies
Bearer ID tokens, Supabase PostgreSQL stores item records, and Render runs the
service.

## Local development

Copy `.env.example` to `.env` and set the local secrets there. The API loads
`.env` at startup; deployment secrets should be configured in the hosting
provider instead.

```bash
python3.11 -m venv .venv
./.venv/bin/python -m pip install -r requirements.txt
./.venv/bin/python -c "import email_validator; print('email-validator is installed')"
./.venv/bin/python -m uvicorn main:app --env-file .env --reload --no-access-log
```

Required environment variables:

- `DATABASE_URL`: Supabase PostgreSQL connection string. The `vector` extension
	must be enabled in the Supabase project.
- `FIREBASE_SERVICE_ACCOUNT_JSON`, or `GOOGLE_APPLICATION_CREDENTIALS`: Firebase
	Admin credentials used to verify Android ID tokens.
- `APP_SECRET_KEY`: a private value of at least 32 characters used to protect
	verification challenges, rate-limit identifiers, and SafeTrade IMEI hashes.
	Keep this value stable: changing it makes existing SafeTrade IMEI hashes
	impossible to compare with new lookups.
- `PUBLIC_BASE_URL`: Public API URL used in uploaded image URLs.
- `CORS_ORIGINS`: Comma-separated browser origins allowed to call the API.
  Defaults to the production API origin; wildcard entries are ignored in
  production and logged. Bearer-token authentication is used instead of cookies.
- `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, and `SUPABASE_STORAGE_BUCKET`:
	configure these to store uploads permanently in a public Supabase Storage
	bucket and clean up matching-index data when deleting reports. The
	service-role key must remain server-side only; report-bearing account
	deletion fails safely if the cleanup credentials are unavailable.
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET`
	enable deletion of Fendly-managed Cloudinary images during account/report
	cleanup. Keep the API secret server-side only.
- `IMAGE_MATCHING_FUNCTION_URL` points the API to the authenticated Firebase
  Cloud Function that indexes Cloudinary report images and performs CLIP visual
  matching. Deploy it with `firebase deploy --only functions:matchReportImages`.
- Set `FAST2SMS_API_KEY` to enable Indian SMS verification through Fast2SMS's
  `bulkV2` Quick SMS (`q`) route. Keep the key server-side in `.env` locally and in the
  deployment environment. OTPs are generated and verified by the backend,
  stored as keyed digests, limited per phone and source IP, and expire after
  10 minutes. Verification attempts are capped. Successful verification
  returns a short-lived phone-verification JWT, not an account-login token.
- Matching is local and free: keyword similarity is combined with location
	proximity. OpenAI is optional; if absent, the app falls back to a local
	free sentence-transformers model.
- `GEMINI_API_KEY` enables English translations of non-English reports for
	admin views and is preferred when both provider keys are set. `OPENAI_API_KEY`
	is supported as a fallback. Original report text stays unchanged for users;
	translations are stored separately and older reports are backfilled when
	viewed. Configure `GEMINI_TRANSLATION_MODEL` (default `gemini-3.8-flash`;
	retries transient provider overload with `gemini-3.1-flash-lite`) or
	`OPENAI_TRANSLATION_MODEL` (default `gpt-4o-mini`) as applicable. Provider
	usage may incur charges. Without a working key, admins see an explicit
	translation-unavailable message instead of untranslated text.

Uploads use Supabase Storage when configured. Without those variables, local
development writes to `static/uploads`; Render's free filesystem is ephemeral.

## API

- `GET /health`
- `POST /api/upload`
- `POST /api/items/lost`
- `POST /api/items/found`
- Optional `imei_number` on a lost-device report: enables SafeTrade matching.
  The API accepts exactly 15 digits and stores only a keyed HMAC digest; it
  never returns or logs the raw IMEI.
- `GET /api/v1/imei/verify/{imei_number}` (public; limited to 10 lookups per
  client IP per minute; returns only CLEAN/FLAGGED and a generic message).
  Access logging is disabled because the IMEI is part of the URL path.
- `GET /api/items/mine`
- `POST /api/items/match` (report owners can match their own reports; ordinary
  accounts receive a limited potential-match preview, while explicitly listed
  admins can access full report details)
- `DELETE /api/users/account` (deletes account data and its reports)
- `/static/delete-account.html` (external account-deletion request page)
- `GET /api/users/username/{username}`
- `POST /api/users/username`
- `GET /api/admin/items`
- `GET /api/admin/matches/{found_item_id}`
- `POST /api/admin/matches/{found_item_id}/notify`
- `POST /api/devices/fcm-token`
- `DELETE /api/devices/fcm-token`

Protected `/api` endpoints require `Authorization: Bearer <Firebase ID token>`.
SMS send/verify endpoints are unauthenticated for sign-up support and are
protected by persistent rate limits and short-lived challenges. Admin access
requires an explicit Firebase UID in `ADMIN_FIREBASE_UIDS`; wildcard
configuration is not accepted. Set `OPENAI_API_KEY` to enable provider-backed
moderation; without it, the backend uses its local safety blocklist.

Apply `supabase/migrations/20261004090000_safetrade_imei_verification.sql`
before deploying SafeTrade. It adds a status and keyed-IMEI-hash index for
lost reports and the persistent public lookup rate-limit table.

## Purge reports retained for previously deleted accounts

After deploying, run the following from the production Render Shell or an
authorized local terminal configured with production credentials. The first
command only reports counts. It requires `ENVIRONMENT=production` and a
PostgreSQL `DATABASE_URL`; `--apply` additionally requires Firebase and
Supabase cleanup credentials. The script refuses to use its local SQLite
fallback. The second command permanently removes legacy reports marked
`account-deleted`, linked SQL/Supabase match data and notifications, Firestore
report copies, and unshared images stored by configured providers:

```bash
python -m scripts.purge_account_deleted_reports
python -m scripts.purge_account_deleted_reports --apply
```

The purge requires production database, Firebase, and Supabase credentials. If
any report image is hosted in Cloudinary, configure its cloud name, API key,
and API secret before running `--apply`; the script refuses to start the purge
when those Cloudinary credentials are missing. Do not set only
`ENVIRONMENT=production`, and do not use development database credentials.
