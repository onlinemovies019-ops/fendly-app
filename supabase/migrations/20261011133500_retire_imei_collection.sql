BEGIN;

ALTER TABLE IF EXISTS public.vault_items
    ADD COLUMN IF NOT EXISTS serial_number varchar(128);

DO $$
BEGIN
    IF to_regclass('public.vault_items') IS NOT NULL
       AND EXISTS (
           SELECT 1
           FROM information_schema.columns
           WHERE table_schema = 'public'
             AND table_name = 'vault_items'
             AND column_name = 'serial_imei'
       ) THEN
        UPDATE public.vault_items
        SET serial_number = CASE
            WHEN btrim(serial_imei) ~ '^[0-9]{15}$' THEN NULL
            WHEN position('/' IN serial_imei) > 0 THEN
                CASE
                    WHEN btrim(split_part(serial_imei, '/', 1)) ~ '^[0-9]{15}$'
                         AND btrim(substr(serial_imei, position('/' IN serial_imei) + 1)) ~ '^[0-9]{15}$'
                        THEN NULL
                    WHEN btrim(split_part(serial_imei, '/', 1)) ~ '^[0-9]{15}$'
                        THEN NULLIF(btrim(substr(serial_imei, position('/' IN serial_imei) + 1)), '')
                    WHEN btrim(substr(serial_imei, position('/' IN serial_imei) + 1)) ~ '^[0-9]{15}$'
                        THEN NULLIF(btrim(split_part(serial_imei, '/', 1)), '')
                    ELSE NULLIF(btrim(serial_imei), '')
                END
            ELSE NULLIF(btrim(serial_imei), '')
        END
        WHERE serial_imei IS NOT NULL
          AND (serial_number IS NULL OR serial_number = serial_imei);

        ALTER TABLE public.vault_items
            DROP COLUMN IF EXISTS serial_imei;
    END IF;
END
$$;

ALTER TABLE IF EXISTS public.lost_items
    DROP COLUMN IF EXISTS imei_hash;

DROP INDEX IF EXISTS public.lost_items_status_imei_hash_idx;
DROP TABLE IF EXISTS public.public_imei_lookup_rate_limits;

COMMIT;
