from enum import Enum
from pydantic import BaseModel, Field
from typing import Optional


class Priority(str, Enum):
    HIGH   = "HIGH"
    MEDIUM = "MEDIUM"
    LOW    = "LOW"


class LeadScoringRequest(BaseModel):
    industry: str = Field(..., description="e.g. TECHNOLOGY, FINANCE, HEALTHCARE")
    company_size: str = Field(..., description="ENTERPRISE | MID_MARKET | SMB | STARTUP")
    engagement_score: float = Field(..., ge=0, le=10, description="0–10 engagement rating")
    past_interactions: int = Field(..., ge=0, description="Number of past interactions")
    additional_context: Optional[str] = Field(default=None, description="Free-text notes about the lead")


class LeadScoringResponse(BaseModel):
    lead_score: int = Field(..., ge=0, le=100)
    priority: Priority
    reasoning: str
    rule_score: int
    similarity_score: int
