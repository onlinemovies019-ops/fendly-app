BEGIN;

ALTER TABLE public.lost_items
    ADD COLUMN IF NOT EXISTS social_poster_url varchar(1000);

ALTER TABLE public.found_items
    ADD COLUMN IF NOT EXISTS social_poster_url varchar(1000);

COMMIT;
