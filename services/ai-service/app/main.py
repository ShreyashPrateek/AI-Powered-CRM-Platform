"""
CRM AI Service — FastAPI application
"""
from contextlib import asynccontextmanager
import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from prometheus_fastapi_instrumentator import Instrumentator

from app.core.config import get_settings
from app.core.logging import setup_logging
from app.api.router import api_router
from app.models.registry import registry

setup_logging()
logger = logging.getLogger(__name__)
settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    # ── Startup ───────────────────────────────────────────────────────────────
    logger.info("Starting %s v%s", settings.APP_NAME, settings.APP_VERSION)
    registry.load()   # Load all HuggingFace models once
    yield
    # ── Shutdown ──────────────────────────────────────────────────────────────
    logger.info("Shutting down %s", settings.APP_NAME)


app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description=(
        "AI microservice powering the CRM platform. "
        "Provides email reply generation, lead scoring, "
        "sentiment analysis, and a RAG-based CRM chatbot."
    ),
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

# ── CORS ──────────────────────────────────────────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Prometheus metrics ────────────────────────────────────────────────────────
Instrumentator().instrument(app).expose(app, endpoint="/actuator/prometheus")

# ── Routes ────────────────────────────────────────────────────────────────────
app.include_router(api_router)


@app.get("/actuator/health", tags=["Health"])
def health() -> dict:
    return {
        "status": "UP",
        "models_loaded": registry._loaded,
        "generation_model": settings.GENERATION_MODEL,
        "sentiment_model": settings.SENTIMENT_MODEL,
        "embedding_model": settings.EMBEDDING_MODEL,
    }
