DROP INDEX IF EXISTS public.lost_items_imei_idx;
DROP INDEX IF EXISTS public.found_items_imei_idx;

ALTER TABLE IF EXISTS public.lost_items
    DROP COLUMN IF EXISTS imei;

ALTER TABLE IF EXISTS public.found_items
    DROP COLUMN IF EXISTS imei;
