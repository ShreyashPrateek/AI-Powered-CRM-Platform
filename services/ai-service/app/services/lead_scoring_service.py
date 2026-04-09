"""
LeadScoringService
──────────────────
Hybrid lead scoring:
  1. Rule-based signal scoring  (industry, company size, engagement, interactions)
  2. Embedding similarity boost (cosine similarity to known high-value lead profiles)

Final score = weighted blend of both components → 0–100.
Priority is derived from the final score bucket.
"""
import logging
from typing import Optional

import numpy as np
import re

from app.models.registry import registry
from app.schemas.lead_scoring import LeadScoringRequest, LeadScoringResponse, Priority

_LOG_SANITIZE = re.compile(r"[\r\n\t]")


def _safe(value: str) -> str:
    """Strip newlines/tabs from user input before logging to prevent log injection."""
    return _LOG_SANITIZE.sub(" ", str(value))

logger = logging.getLogger(__name__)

# ── Industry weights (based on typical B2B deal sizes) ────────────────────────
_INDUSTRY_SCORE: dict[str, int] = {
    "TECHNOLOGY":    25,
    "FINANCE":       23,
    "HEALTHCARE":    20,
    "REAL_ESTATE":   18,
    "MANUFACTURING": 16,
    "MEDIA":         14,
    "RETAIL":        12,
    "EDUCATION":     10,
    "OTHER":          8,
}

# ── Company size weights ──────────────────────────────────────────────────────
_SIZE_SCORE: dict[str, int] = {
    "ENTERPRISE":   25,   # 1000+
    "MID_MARKET":   20,   # 100–999
    "SMB":          12,   # 10–99
    "STARTUP":       8,   # <10
}

# ── Reference profiles of historically closed-won leads ───────────────────────
# These are embedded at scoring time to compute similarity.
_HIGH_VALUE_PROFILES = [
    "Enterprise technology company with high engagement and multiple interactions",
    "Finance sector mid-market company with strong product fit and quick responses",
    "Healthcare enterprise with budget confirmed and decision maker engaged",
    "Manufacturing company with existing vendor relationship and renewal interest",
]


class LeadScoringService:
    def __init__(self) -> None:
        self._reference_embeddings: Optional[np.ndarray] = None

    def score(self, req: LeadScoringRequest, user_id: Optional[str] = None) -> LeadScoringResponse:
        rule_score = self._rule_score(req)
        similarity_score = self._similarity_score(req, user_id)

        # Weighted blend: 60% rules, 40% semantic similarity
        final_score = round(rule_score * 0.6 + similarity_score * 0.4)
        final_score = max(0, min(100, final_score))

        priority = self._to_priority(final_score)
        reasoning = self._build_reasoning(req, rule_score, similarity_score, final_score)

        logger.info(
            "Lead scored | industry=%s | size=%s | score=%d | priority=%s",
            _safe(req.industry), _safe(req.company_size), final_score, priority,
        )

        return LeadScoringResponse(
            lead_score=final_score,
            priority=priority,
            reasoning=reasoning,
            rule_score=round(rule_score),
            similarity_score=round(similarity_score),
        )

    # ── Rule-based scoring (0–100) ────────────────────────────────────────────

    def _rule_score(self, req: LeadScoringRequest) -> float:
        score = 0.0

        # Industry (max 25)
        score += _INDUSTRY_SCORE.get(req.industry.upper(), 8)

        # Company size (max 25)
        score += _SIZE_SCORE.get(req.company_size.upper(), 8)

        # Engagement score provided by caller (0–10 → scaled to 0–30)
        engagement = max(0, min(10, req.engagement_score))
        score += engagement * 3.0

        # Past interactions (each interaction worth 2 pts, max 20)
        score += min(req.past_interactions * 2, 20)

        return score  # max theoretical = 100

    # ── Embedding similarity scoring (0–100) ──────────────────────────────────

    def _similarity_score(self, req: LeadScoringRequest, user_id: Optional[str] = None) -> float:
        if registry.embedding_model is None:
            return 50.0  # neutral fallback

        # Validate and sanitize input before embedding — prevents data poisoning
        industry = re.sub(r"[^A-Z_]", "", req.industry.upper())[:30]
        company_size = re.sub(r"[^A-Z_]", "", req.company_size.upper())[:20]
        engagement = max(0.0, min(10.0, float(req.engagement_score)))
        interactions = max(0, min(1000, int(req.past_interactions)))
        extra = re.sub(r"[<>\"']", "", req.additional_context or "")[:200]

        lead_text = (
            f"{company_size} company in {industry} industry "
            f"with engagement score {engagement}/10 "
            f"and {interactions} past interactions. "
            f"{extra}"
        ).strip()

        lead_emb = registry.embedding_model.encode([lead_text], normalize_embeddings=True)

        # Lazy-load reference embeddings
        if self._reference_embeddings is None:
            self._reference_embeddings = registry.embedding_model.encode(
                _HIGH_VALUE_PROFILES, normalize_embeddings=True
            )

        # Cosine similarity (embeddings are already L2-normalised → dot product = cosine)
        similarities = np.dot(self._reference_embeddings, lead_emb.T).flatten()
        max_similarity = float(similarities.max())

        # Map [-1, 1] cosine range → [0, 100]
        return (max_similarity + 1) / 2 * 100

    # ── Helpers ───────────────────────────────────────────────────────────────

    @staticmethod
    def _to_priority(score: int) -> Priority:
        if score >= 75:
            return Priority.HIGH
        if score >= 45:
            return Priority.MEDIUM
        return Priority.LOW

    @staticmethod
    def _build_reasoning(
        req: LeadScoringRequest,
        rule_score: float,
        similarity_score: float,
        final_score: int,
    ) -> str:
        lines = [
            f"Industry ({req.industry}): contributes {_INDUSTRY_SCORE.get(req.industry.upper(), 8)} pts.",
            f"Company size ({req.company_size}): contributes {_SIZE_SCORE.get(req.company_size.upper(), 8)} pts.",
            f"Engagement score {req.engagement_score}/10: contributes {min(req.engagement_score, 10) * 3} pts.",
            f"Past interactions ({req.past_interactions}): contributes {min(req.past_interactions * 2, 20)} pts.",
            f"Semantic similarity to closed-won profiles: {round(similarity_score)}%.",
            f"Final blended score: {final_score}/100.",
        ]
        return " ".join(lines)


lead_scoring_service = LeadScoringService()
