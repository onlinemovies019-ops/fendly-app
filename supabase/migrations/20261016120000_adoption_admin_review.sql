BEGIN;

ALTER TABLE public.adoption_applications
    ADD COLUMN IF NOT EXISTS owner_approved boolean NOT NULL DEFAULT false;

COMMIT;
