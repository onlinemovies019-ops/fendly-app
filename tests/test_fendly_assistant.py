import httpx
import pytest
from fastapi.testclient import TestClient

import main
from fendly_assistant import (
    AssistantChatRequest,
    AssistantMessage,
    generate_fendly_answer,
)
from routers import assistant as assistant_module


@pytest.mark.asyncio
async def test_gemini_assistant_sends_grounded_conversation(monkeypatch):
    monkeypatch.setenv("GEMINI_API_KEY", "test-key")
    monkeypatch.setenv("GEMINI_ASSISTANT_MODEL", "gemini-test")
    captured = {}

    class FakeResponse:
        status_code = 200

        def raise_for_status(self):
            return None

        def json(self):
            return {
                "candidates": [{
                    "content": {"parts": [{"text": "Open My Reports to check status."}]}
                }]
            }

    class FakeAsyncClient:
        def __init__(self, **kwargs):
            pass

        async def __aenter__(self):
            return self

        async def __aexit__(self, *args):
            pass

        async def post(self, endpoint, **kwargs):
            captured["endpoint"] = endpoint
            captured["headers"] = kwargs["headers"]
            captured["body"] = kwargs["json"]
            return FakeResponse()

    monkeypatch.setattr("fendly_assistant.httpx.AsyncClient", FakeAsyncClient)
    response = await generate_fendly_answer(AssistantChatRequest(
        question="How do I check my report?",
        history=[
            AssistantMessage(role="user", content="I submitted a report"),
            AssistantMessage(role="assistant", content="You can check it in My Reports."),
        ],
    ))

    assert response == "Open My Reports to check status."
    assert captured["endpoint"].endswith("/models/gemini-test:generateContent")
    assert captured["headers"] == {"x-goog-api-key": "test-key"}
    assert captured["body"]["systemInstruction"]["parts"][0]["text"].startswith(
        "You are Fendly Help"
    )
    assert captured["body"]["contents"] == [
        {"role": "user", "parts": [{"text": "I submitted a report"}]},
        {"role": "model", "parts": [{"text": "You can check it in My Reports."}]},
        {"role": "user", "parts": [{"text": "How do I check my report?"}]},
    ]


@pytest.mark.asyncio
async def test_gemini_assistant_retries_configured_fallback(monkeypatch):
    monkeypatch.setenv("GEMINI_API_KEY", "test-key")
    monkeypatch.setenv("GEMINI_ASSISTANT_MODEL", "primary")
    monkeypatch.setenv("GEMINI_ASSISTANT_FALLBACK_MODEL", "fallback")
    endpoints = []

    class FakeResponse:
        def __init__(self, status_code):
            self.status_code = status_code

        def raise_for_status(self):
            if self.status_code >= 400:
                request = httpx.Request("POST", "https://example.test")
                response = httpx.Response(self.status_code, request=request)
                raise httpx.HTTPStatusError("provider busy", request=request, response=response)

        def json(self):
            return {"candidates": [{
                "content": {"parts": [{"text": "Fendly answer"}]}
            }]}

    class FakeAsyncClient:
        def __init__(self, **kwargs):
            pass

        async def __aenter__(self):
            return self

        async def __aexit__(self, *args):
            pass

        async def post(self, endpoint, **kwargs):
            endpoints.append(endpoint)
            return FakeResponse(503 if len(endpoints) == 1 else 200)

    monkeypatch.setattr("fendly_assistant.httpx.AsyncClient", FakeAsyncClient)
    answer = await generate_fendly_answer(AssistantChatRequest(question="How do I report?"))

    assert answer == "Fendly answer"
    assert endpoints == [
        "https://generativelanguage.googleapis.com/v1beta/models/primary:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/fallback:generateContent",
    ]


def test_assistant_endpoint_requires_authentication():
    client = TestClient(main.app)
    response = client.post(
        "/api/assistant/chat",
        json={"question": "How do I use Fendly?"},
    )

    assert response.status_code == 401


def test_assistant_endpoint_returns_gemini_answer_for_authenticated_user(monkeypatch):
    async def fake_answer(payload):
        assert payload.question == "How do I use Fendly?"
        return "Ask Fendly questions here."

    monkeypatch.setattr(assistant_module, "generate_fendly_answer", fake_answer)
    main.app.dependency_overrides[assistant_module.get_current_user] = lambda: "test-user"
    try:
        client = TestClient(main.app)
        response = client.post(
            "/api/assistant/chat",
            headers={"Authorization": "Bearer test-token"},
            json={"question": "How do I use Fendly?"},
        )
    finally:
        main.app.dependency_overrides.pop(assistant_module.get_current_user, None)

    assert response.status_code == 200
    assert response.json() == {"answer": "Ask Fendly questions here."}


def test_assistant_request_limits_history_and_question_length():
    with pytest.raises(ValueError):
        AssistantChatRequest(question="x" * 1001)
    with pytest.raises(ValueError):
        AssistantChatRequest(
            question="Help",
            history=[AssistantMessage(role="user", content="question")] * 9,
        )
