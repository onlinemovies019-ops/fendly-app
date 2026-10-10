BEGIN;

ALTER TABLE public.social_accounts
    ADD COLUMN IF NOT EXISTS deletion_access_token_encrypted text;

COMMIT;
