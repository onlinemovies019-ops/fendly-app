from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi import APIRouter

app = FastAPI(title="Fendly API")

router = APIRouter(prefix="/api")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["Authorization", "Content-Type", "Accept", "Origin"],
)


@router.post("/items")
async def create_item():
    return {"ok": True, "message": "POST /api/items reached"}


@router.post("/items/match")
async def match_items():
    return {"ok": True, "message": "POST /api/items/match reached"}


app.include_router(router)


@app.get("/health")
async def health_check():
    return {"status": "ok"}
