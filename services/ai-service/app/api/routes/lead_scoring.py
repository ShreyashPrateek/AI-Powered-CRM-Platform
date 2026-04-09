from fastapi import APIRouter
from app.schemas.lead_scoring import LeadScoringRequest, LeadScoringResponse
from app.services.lead_scoring_service import lead_scoring_service

router = APIRouter(prefix="/score-lead", tags=["Lead Scoring"])


@router.post("", response_model=LeadScoringResponse, summary="Score a lead")
def score_lead(req: LeadScoringRequest) -> LeadScoringResponse:
    """
    Predict deal success probability for a lead.

    Returns a 0–100 score and HIGH / MEDIUM / LOW priority with reasoning.
    """
    return lead_scoring_service.score(req)
