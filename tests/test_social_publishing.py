from pathlib import Path

import main


def test_social_publishing_module_and_cleanup_route_are_removed():
    repository_root = Path(__file__).resolve().parents[1]

    assert not (repository_root / "social_publishing.py").exists()
    assert not any(
        route.path.startswith("/api/admin/social")
        for route in main.app.routes
    )


def test_production_python_sources_have_no_meta_graph_api_calls():
    repository_root = Path(__file__).resolve().parents[1]
    production_sources = [
        *repository_root.glob("*.py"),
        *repository_root.glob("routers/*.py"),
        *repository_root.glob("scripts/*.py"),
    ]

    assert all(
        "graph.facebook.com" not in source.read_text(encoding="utf-8").lower()
        for source in production_sources
    )
