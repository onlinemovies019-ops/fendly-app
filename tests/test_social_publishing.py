from pathlib import Path

import main


def test_social_publishing_module_and_cleanup_route_are_removed():
    repository_root = Path(__file__).resolve().parents[1]

    assert not (repository_root / "social_publishing.py").exists()
    assert not any(
        route.path.startswith("/api/admin/social")
        for route in main.app.routes
    )


def test_meta_graph_api_calls_are_limited_to_opt_in_adoption_publishing():
    repository_root = Path(__file__).resolve().parents[1]
    adoption_social = repository_root / "adoption_social.py"
    assert "graph.facebook.com" in adoption_social.read_text(encoding="utf-8")

    production_sources = [
        *repository_root.glob("*.py"),
        *repository_root.glob("routers/*.py"),
        *repository_root.glob("scripts/*.py"),
    ]
    for source in production_sources:
        if source == adoption_social:
            continue
        assert "graph.facebook.com" not in source.read_text(encoding="utf-8").lower()

    adoption_router = (repository_root / "routers/adoptions.py").read_text(encoding="utf-8")
    assert "if not listing.auto_post_social" in adoption_router
    assert "publish_adoption_listing" in adoption_router
