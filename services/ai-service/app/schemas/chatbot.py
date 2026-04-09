from pydantic import BaseModel, Field
from typing import Any, Optional


class SourceDocument(BaseModel):
    content: str
    metadata: dict[str, Any]
    relevance_score: float
    source: str  # "deals" | "leads"


class ChatRequest(BaseModel):
    question: str = Field(..., min_length=3, description="Natural language CRM question")
    session_id: Optional[str] = Field(default=None, description="Optional session ID for future multi-turn support")


class ChatResponse(BaseModel):
    answer: str
    sources: list[SourceDocument]
    model_used: str


# ── Ingestion schemas ─────────────────────────────────────────────────────────

class IngestDealsRequest(BaseModel):
    deals: list[dict[str, Any]] = Field(..., min_length=1)


class IngestLeadsRequest(BaseModel):
    leads: list[dict[str, Any]] = Field(..., min_length=1)


class IngestResponse(BaseModel):
    upserted: int
