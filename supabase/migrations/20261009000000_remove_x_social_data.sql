BEGIN;

DELETE FROM public.social_oauth_states
WHERE provider = 'x';

DELETE FROM public.social_publications
WHERE provider = 'x';

DELETE FROM public.social_accounts
WHERE provider = 'x';

ALTER TABLE public.users
    DROP COLUMN IF EXISTS x_url;

COMMIT;
