ALTER TABLE IF EXISTS public.lost_items
    ADD COLUMN IF NOT EXISTS imei varchar(32);

ALTER TABLE IF EXISTS public.found_items
    ADD COLUMN IF NOT EXISTS imei varchar(32);

CREATE INDEX IF NOT EXISTS lost_items_imei_idx
    ON public.lost_items (imei);

CREATE INDEX IF NOT EXISTS found_items_imei_idx
    ON public.found_items (imei);
