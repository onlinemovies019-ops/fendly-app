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
- `OPENAI_API_KEY`: Required in production for fail-closed moderation of report
  text and report images before upload/publication. Moderation uses
  `OPENAI_MODERATION_MODEL` (default `omni-moderation-latest`) and
  `OPENAI_MODERATION_URL` (default OpenAI's moderation endpoint). If the key is
  missing or the provider is unavailable, submissions are rejected with a
  temporary-service error; there is no keyword-only fallback.
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
- Optional brand-account publishing uses `META_APP_ID`, `META_APP_SECRET`,
	`META_LOGIN_CONFIG_ID`, `META_GRAPH_API_VERSION`, and `META_REDIRECT_URI`.
	Keep every secret in
	the backend host's secret store. Social access tokens are encrypted using
	`APP_SECRET_KEY`; keep that key stable or reconnect the social accounts
	after rotating it.
- `IMAGE_MATCHING_FUNCTION_URL` points the API to the authenticated Firebase
  Cloud Function that indexes Cloudinary report images and performs CLIP visual
  matching. Deploy it with `firebase deploy --only functions:matchReportImages`.
- Set `FAST2SMS_API_KEY` to enable Indian SMS verification through Fast2SMS.
  Keep the key server-side in `.env` locally and in the deployment environment.
  Fendly sends OTPs through Fast2SMS Quick SMS (`q`) by default. To use the DLT
  route instead, register and get approval for the OTP template with Fast2SMS,
  then set `FAST2SMS_OTP_TEMPLATE_ID` to that template ID in the backend
  environment (including Render) and redeploy.
  OTPs are generated and verified by the backend,
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
- `GET /api/social/status`, `GET /api/social/publications`, and
  `POST /api/social/publications/refresh` (admin-only; refresh checks recent
  published post IDs against Meta and reports whether each post is available,
  missing/inaccessible, or could not be checked)
- `POST /api/social/connect/meta` (admin-only;
  return the platform authorization URL)
- `DELETE /api/social/account/{provider}` (admin-only; removes
  Fendly's stored authorization)
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
configuration is not accepted. Alternatively, configure `ADMIN_EMAIL` for a
verified Fendly profile. Content moderation fails closed unless the production
moderation provider is configured and responds successfully.

Apply `supabase/migrations/20261004090000_safetrade_imei_verification.sql`
before deploying SafeTrade. It adds a status and keyed-IMEI-hash index for
lost reports and the persistent public lookup rate-limit table.

## Optional social publishing

Social publishing is disabled until the Fendly administrators connect brand
accounts. Set the provider app credentials and exact HTTPS callback URLs in
the backend environment. Register these callback URLs with the providers:

- `https://<your-api-host>/api/social/callback/meta`

For Meta, create a Login for Business configuration that grants access to the
Fendly Facebook Page and its connected Instagram professional account. Its
Facebook Login for Business permissions need `pages_show_list`,
`pages_read_engagement`, `pages_manage_posts`, `instagram_basic`, and
`instagram_content_publish` (singular). Set its configuration ID as
`META_LOGIN_CONFIG_ID`, set the currently supported Graph API version in
`META_GRAPH_API_VERSION`, and register the Meta callback URL above. If the
authorized Meta user manages multiple Pages, set `META_PAGE_ID` to the Fendly
Page's ID. Fendly's Meta app must be in a mode and have permissions approved
for the people who will authorize it; development or testing access is not
production approval. The Meta app must be a business-type app. Access to
assets managed by people outside the app's roles requires Advanced Access
through Meta App Review. An Instagram professional account must be linked to
the selected Page, and the Page must meet Meta's publishing authorization
requirements. If the Page is assigned through Business Manager, Meta may also
require `ads_read` and `ads_management` for Instagram publishing.

The admin publication refresh reconciles saved Facebook post IDs against the
Page's `published_posts` feed and Instagram media IDs against the account's
`media` feed, including Graph API pagination. A completed feed check removes
jobs whose posts are no longer listed. If feed access or pagination is
inconclusive, Fendly checks each missing post ID directly. If Meta still cannot
confirm the post's status, the job remains unknown and is not treated as
deleted; check the connection and Meta permissions before relying on the result.

After configuring secrets and deploying the backend, an authorized Fendly
administrator can call `POST /api/social/connect/meta` with a Fendly bearer
token, open the returned
`authorization_url`, and approve the brand account. Use the status and
publication endpoints to verify connections and review failed jobs. Refresh
can identify Meta posts that are no longer available, but Meta may not
distinguish deletion from changed access permissions. Deleting
or disconnecting an authorization in Fendly does not revoke it in Meta; also
revoke Fendly from the provider's app settings.

Each report has an unchecked opt-in. A consented report publishes its type and
title plus a link to that specific report (`FENDLY_APP_URL/item/{report_id}`;
the base defaults to `https://fendly.app`). Facebook and Instagram receive a generated JPEG
community poster containing the title, report type, optional selected photo,
and report link. Photos fit inside a square frame without cropping; the report
label sits beside the QR code below the photo. The caption ends with
`Fendly: <report URL>`. The description,
date, location, contact
details, and IMEI are not included. The poster is stored with report media and
is removed during account deletion when no other report references it. This
version creates standard photo posts, not Reels, because reports have no video
source. Poster generation and provider publishing run after the report is saved;
use the status and publication endpoints to track progress or inspect failures.
Jobs previously failed only because a poster was unavailable are safely queued
again by the publication worker.
Already published posts are public copies on
Fendly's brand accounts and are not automatically removed when the reporter
deletes their Fendly account. The privacy policy and account-deletion page
describe this and provide the support contact for removal requests.

Before deploying social publishing, apply
`supabase/migrations/20261008000000_social_publishing.sql` to the production
Supabase database. This adds the consent columns and social tables, enables
row-level security, and removes direct access for `anon` and `authenticated`.
Apply `supabase/migrations/20261008213000_social_poster_urls.sql` as well to
store generated poster URLs. Set `FENDLY_APP_URL` to the public app/download
landing page once that URL is confirmed.
Apply the migration before deploying the API version that reads or writes these
columns. Publishing attempts that fail or are interrupted are not automatically
retried because the provider may already have created a public post; check the
provider account before taking any manual retry action.

To permanently remove legacy X data left by older versions, apply
`supabase/migrations/20261009000000_remove_x_social_data.sql`. It deletes X
authorization, OAuth-state, and publication-history rows and drops the old
`users.x_url` column. This does not remove posts already published on X; those
remain controlled by the X account. Remove any leftover X API credentials from
the production hosting environment separately.

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
