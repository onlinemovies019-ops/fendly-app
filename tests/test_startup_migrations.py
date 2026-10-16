from pathlib import Path

import main


def test_private_workflow_migration_runs_schema_and_storage_policies():
    statements = []

    class FakeConnection:
        def exec_driver_sql(self, statement):
            statements.append(statement)

    main._apply_private_workflow_migration(FakeConnection())

    sql = "\n".join(statements)
    assert "ALTER TABLE public.lost_items ENABLE ROW LEVEL SECURITY" in sql
    assert "private_image_migration_cleanup" not in sql
    assert "INSERT INTO storage.buckets" in sql
    assert "'report-images'" in sql
    assert "fendly_private_report_images_deny_clients" in sql
    assert "'private-vault'" in sql
    assert "fendly_private_vault_deny_clients" in sql
    assert "BEGIN" not in statements
    assert "COMMIT" not in statements


def test_adoption_migration_creates_private_api_tables_and_public_photo_bucket():
    repository_root = Path(__file__).resolve().parents[1]
    migration = (
        repository_root
        / "supabase/migrations/20261011150000_add_adoption_module.sql"
    ).read_text(encoding="utf-8")

    assert "CREATE TABLE IF NOT EXISTS public.adoption_listings" in migration
    assert "CREATE TABLE IF NOT EXISTS public.adoption_applications" in migration
    assert "fendly_private_adoption_listings_deny_clients" in migration
    assert "fendly_private_adoption_applications_deny_clients" in migration
    assert "'adoption-images'" in migration
    assert "public = true" in migration
    assert "found_item_id DROP NOT NULL" in migration


def test_adoption_admin_review_migration_adds_lister_selection_flag():
    repository_root = Path(__file__).resolve().parents[1]
    migration = (
        repository_root
        / "supabase/migrations/20261016120000_adoption_admin_review.sql"
    ).read_text(encoding="utf-8")

    assert "ADD COLUMN IF NOT EXISTS owner_approved boolean NOT NULL DEFAULT false" in migration
