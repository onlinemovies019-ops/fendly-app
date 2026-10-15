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
  verification challenges and rate-limit identifiers. Keep this value stable
  to preserve verification and rate-limit integrity.
- `PUBLIC_BASE_URL`: Public API URL used for Fendly link generation.
- `OPENAI_API_KEY`: Required in production for fail-closed moderation of report
  text and report images before storage. Moderation uses
  `OPENAI_MODERATION_MODEL` (default `omni-moderation-latest`) for text; image
  moderation always uses the multimodal `omni-moderation-latest` model, with
  one image per provider request.
  `OPENAI_MODERATION_URL` defaults to OpenAI's moderation endpoint. If the key
  is missing or the provider is unavailable, submissions are rejected with a
  temporary-service error; there is no keyword-only fallback.
- `CORS_ORIGINS`: Comma-separated browser origins allowed to call the API.
  Defaults to the production API origin; wildcard entries are ignored in
  production and logged. Bearer-token authentication is used instead of cookies.
- `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY`: configure private image
	uploads, signed URLs, and matching-index cleanup. The service-role key must
	remain server-side only. The API creates and verifies the private
	`report-images` bucket during startup.
- `USE_LOCAL_IMAGE_EMBEDDING_MODEL=true`: enable image embeddings using the
  backend's local MobileCLIP-S1 ONNX model. The model and its weights are
  downloaded by the backend, not by Firebase Cloud Functions.
- `VAULT_STORAGE_BUCKET` (default `private-vault`) and
  `VAULT_ENCRYPTION_KEY`: configure the private object bucket and base64-encoded
  32-byte AES-256 key used for encrypted vault images. Keep the key stable and
  server-side; changing it makes existing vault images undecryptable.
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET`
  enable deletion of Fendly-managed legacy Cloudinary images during account
  cleanup. Keep the API secret server-side only.
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
- The authenticated `/api/assistant/chat` endpoint uses the same server-side
	`GEMINI_API_KEY` to answer Fendly app-help questions. It never exposes the key
	to Android, limits each question to 1,000 characters and conversation
	context to eight messages, and sends the selected app language (English,
	Hindi, Marathi, Gujarati, Bengali, Tamil, Telugu, Kannada, or Malayalam) so
	replies match the user's native or Romanized script and tone while keeping
	technical terms in English and localizing Fendly feature and navigation
	labels. It uses
	`GEMINI_ASSISTANT_MODEL` and
	`GEMINI_ASSISTANT_FALLBACK_MODEL` to select Gemini models. Assistant requests
	are provider usage and may consume free-tier quota or incur charges according
	to the Google AI Studio account's current plan and limits. If Gemini is
	misconfigured or unavailable, the endpoint reports an error to the app rather
	than returning a fabricated/offline answer.

Uploads require Supabase Storage. Report images and encrypted vault images use
separate private buckets; the API blocks public access to legacy
`/static/uploads` files. The service does not read or write local upload files.
Legacy `/static/uploads/` references require recovery from a backup and manual
upload to private Supabase Storage.

### Local visual search

Visual search generates 512-dimensional image vectors locally with
MobileCLIP-S1 ONNX; Firebase Cloud Functions and Cloud Vision are not required.
`render.yaml` enables this mode. The first visual-search request downloads the
pinned 86 MB ONNX model from Hugging Face and can take longer while it
initializes. The model is SHA-256 verified, and the ONNX CPU runtime is limited
to one thread. A local smoke test peaked at about 247 MiB of process memory.
The model is distributed under Apple's MobileCLIP license; review the
[upstream license](https://huggingface.co/Xenova/mobileclip_s1/blob/main/LICENSE)
before redistributing model files.

Image embeddings are generated after report creation so model loading does not
delay report submission. Each stored image vector records its model version;
the existing My Reports visual-search flow automatically re-indexes old or
stale vectors in batches of five and reports whether results are incomplete.
The authenticated upload API
`POST /api/items/{lost|found}/visual-search` accepts JPEG, PNG, or WebP
multipart image uploads and returns at most 10 report IDs with cosine
similarity scores. It indexes up to five stale active reports per request
before searching.

## API

- `GET /health`
- `POST /api/upload`
- `POST /api/items/lost`
- `POST /api/items/found`
- Device IMEI collection and SafeTrade verification are disabled. The retired
  `GET /api/v1/imei/verify/{imei_number}` path returns HTTP 410.
- `GET /api/items/mine` (authenticated owner-only reports)
- `POST /api/items/{lost|found}/visual-search` (authenticated multipart image
  upload; searches active opposite-type reports and returns up to 10 IDs with
  cosine similarity scores; requires local CLIP to be enabled)
- `POST /api/items/{lost|found}/{report_id}/visual-matches` (owner-only visual
  search for an active report with a photo; returns at most 10 limited
  possible-match previews and the number of reports not yet indexed)
- `POST /api/items/match` (admin-only matching)
- `GET /api/notifications` and `POST /api/notifications/read` (owner-scoped
  in-app notification inbox)
- `GET /api/admin/alerts` and `POST /api/admin/alerts/{alert_id}/review`
  (admin-only match review; confirmation requires manually verified proof)
- `GET /api/admin/success-stories` (admin-only; returns only reunited reports
  whose owners opted in, with category, profile city, and a temporary signed
  photo URL)
- `PATCH /api/items/{lost|found}/{report_id}/resolution` (owner-only reunion
  status and optional success-story consent)
- `DELETE /api/users/account` (deletes account data and its reports)
- `/static/delete-account.html` (external account-deletion request page)
- `GET /api/users/username/{username}`
- `POST /api/users/username`
- `GET /api/admin/items/summary` and `GET /api/admin/items/browse?type=LOST&offset=0&limit=30`
  (admin-only; lightweight report counts and paginated report browsing)
- `GET /api/admin/items` (admin-only full legacy report export)
- `GET /api/admin/adoptions` and `DELETE /api/admin/adoptions/{listing_id}`
- `GET /api/admin/matches/{found_item_id}`
- `POST /api/admin/matches/{found_item_id}/notify`
- `POST /api/devices/fcm-token`
- `DELETE /api/devices/fcm-token`
- `GET /api/adoptions` (public, filterable pet adoption feed)
- `POST /api/adoptions` and `POST /api/adoptions/{id}/apply` (authenticated)
- `GET /api/adoptions/{id}/applications` (lister-only application review)
- `POST /api/adoptions/applications/{id}/decision` (lister-only decision)

Protected `/api` endpoints require `Authorization: Bearer <Firebase ID token>`.
SMS send/verify endpoints are unauthenticated for sign-up support and are
protected by persistent rate limits and short-lived challenges. Admin access
requires an explicit Firebase UID in `ADMIN_FIREBASE_UIDS`; wildcard
configuration is not accepted. Alternatively, configure `ADMIN_EMAIL` for a
verified Fendly profile. Content moderation fails closed unless the production
moderation provider is configured and responds successfully.
Admin adoption deletion removes the recorded Facebook and Instagram posts
before deleting the listing; if Meta refuses either deletion, the listing is
retained so an administrator can resolve the issue and retry.

As a coordinated release, apply
`supabase/migrations/20261011133500_retire_imei_collection.sql` and deploy the
updated backend and app. The migration removes stored lost-report IMEI hashes,
removes the retired lookup quota table, and preserves non-IMEI Vault serial text
where identifiable.

Apply `supabase/migrations/20261011150000_add_adoption_module.sql` before
deploying the adoption feature. It creates API-only adoption tables and the
public `adoption-images` bucket; Lost/Found photos remain private in
`report-images`. Adoption auto-posting uses the Facebook Page and Instagram account saved
through the admin workspace's **Connect / reconnect Facebook and Instagram**
control. Reconnecting in Fendly refreshes the encrypted saved connection and
takes precedence over any legacy `META_PAGE_ACCESS_TOKEN` fallback in Render.
If no saved connection is present, direct Render credentials may still be used.
Saved access tokens are encrypted with the existing `APP_SECRET_KEY`; keep that
key stable or reconnect the accounts after rotating it. The Graph API version defaults to
`v23.0`; set
`APP_DOWNLOAD_URL` only if the app's Play Store link differs from the default.
For OAuth setup, configure `META_APP_ID`, `META_APP_SECRET`,
`META_LOGIN_CONFIG_ID`, `APP_SECRET_KEY`, and an HTTPS `PUBLIC_BASE_URL` or
`META_REDIRECT_URI`; register the resulting `/api/social/callback/meta` URL in
the Meta app. `META_PAGE_ID` can select the intended Page if the Meta account
has multiple Pages. For direct fallback configuration, set
`META_FACEBOOK_PAGE_ID` (or its `META_PAGE_ID` alias) and
`META_PAGE_ACCESS_TOKEN`. `META_INSTAGRAM_ACCOUNT_ID` may be set to the
numeric Instagram professional account ID; when it is missing or malformed,
the API discovers the account linked to `META_PAGE_ID`. Never put an access
token in either ID variable. The Page token and Meta app must have the
`pages_manage_posts` permission for Facebook and `instagram_basic` plus
`instagram_content_publish` for Instagram. The app ID, app secret, Graph API
version, and login configuration ID do not replace these granted permissions
or the Page access token. If the lister opts in but publishing is unavailable
or fails, the adoption listing remains available and returns a separate
social-post status for retry. Meta access tokens can expire or be revoked;
Fendly cannot renew a token after Meta invalidates it. Reconnect Meta from the
admin workspace to replace the saved server-side credentials, then retry the
listing's social post through the admin retry endpoint.

The FastAPI startup applies
`supabase/migrations/20261011000000_private_match_workflow.sql` to PostgreSQL
idempotently, then verifies that the private Supabase `report-images` bucket is
available. Production startup requires `DATABASE_URL`, `SUPABASE_URL`, and
`SUPABASE_SERVICE_ROLE_KEY`. The migration denies client-role access to report
tables and image buckets.

## Private reports and verified matches

Report APIs require Firebase authentication and scope user list, edit, and
delete operations to the report owner. Potential Lost/Found matches are
available only to configured administrators. An admin must confirm that proof
was manually reviewed and enter verification notes before Fendly creates an
owner-scoped in-app notification and attempts an FCM push. Contact details are
not included in match notifications.

All new report and profile images are uploaded with the Supabase Python client
to the private `report-images` bucket. The database stores a stable object
reference; API responses contain short-lived signed URLs. The service does not
write uploaded images to or read images from a local filesystem.

Fendly does not automatically publish Lost/Theft or Found reports and does
not use Meta OAuth or connect user-owned social accounts. Adoption listers
may explicitly opt in to publishing an adoption listing to the configured
official Facebook Page and Instagram account. Meta credentials are
server-side configuration only; publication failure does not remove the
adoption listing. Reunion-story consent defaults to false and is stored with
each report; opted-in summaries are only available to admins for manual
review and posting outside Fendly. Reopening a report revokes its
success-story consent.

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
