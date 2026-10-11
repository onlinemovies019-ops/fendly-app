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
