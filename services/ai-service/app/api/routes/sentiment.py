from fastapi import APIRouter
from app.schemas.sentiment import SentimentRequest, SentimentResponse
from app.services.sentiment_service import sentiment_service

router = APIRouter(prefix="/sentiment", tags=["Sentiment Analysis"])


@router.post("", response_model=SentimentResponse, summary="Analyse customer sentiment")
def analyse_sentiment(req: SentimentRequest) -> SentimentResponse:
    """
    Analyse the sentiment of a customer email or message using DistilBERT.

    Returns POSITIVE, NEUTRAL, or NEGATIVE with a confidence score.
    """
    return sentiment_service.analyse(req)
