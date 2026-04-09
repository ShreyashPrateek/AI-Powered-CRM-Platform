from enum import Enum
from pydantic import BaseModel, Field


class SentimentLabel(str, Enum):
    POSITIVE = "POSITIVE"
    NEUTRAL  = "NEUTRAL"
    NEGATIVE = "NEGATIVE"


class SentimentRequest(BaseModel):
    text: str = Field(..., min_length=1, description="Customer email or message text to analyse")


class SentimentResponse(BaseModel):
    label: SentimentLabel
    confidence: float = Field(..., ge=0.0, le=1.0)
    raw_label: str
    model_used: str
