"""
SentimentService
────────────────
Analyses customer email text using DistilBERT (SST-2).

DistilBERT SST-2 outputs POSITIVE / NEGATIVE.
We map low-confidence predictions to NEUTRAL to give a 3-class output
that matches the CRM requirement.
"""
import logging

from app.models.registry import registry
from app.schemas.sentiment import SentimentRequest, SentimentResponse, SentimentLabel

logger = logging.getLogger(__name__)

# Confidence threshold below which we call the result NEUTRAL
_NEUTRAL_THRESHOLD = 0.70

# Max characters sent to the model (DistilBERT max tokens = 512)
_MAX_CHARS = 1800


class SentimentService:
    def analyse(self, req: SentimentRequest) -> SentimentResponse:
        text = req.text[:_MAX_CHARS].strip()

        result: list[dict] = registry.sentiment_pipeline(text)
        raw_label: str = result[0]["label"]   # "POSITIVE" or "NEGATIVE"
        confidence: float = result[0]["score"]

        label = self._map_label(raw_label, confidence)

        logger.info(
            "Sentiment analysed | raw=%s | confidence=%.3f | mapped=%s",
            raw_label, confidence, label,
        )

        return SentimentResponse(
            label=label,
            confidence=round(confidence, 4),
            raw_label=raw_label,
            model_used=registry.sentiment_pipeline.model.config.name_or_path,
        )

    @staticmethod
    def _map_label(raw_label: str, confidence: float) -> SentimentLabel:
        if confidence < _NEUTRAL_THRESHOLD:
            return SentimentLabel.NEUTRAL
        return SentimentLabel.POSITIVE if raw_label == "POSITIVE" else SentimentLabel.NEGATIVE


sentiment_service = SentimentService()
