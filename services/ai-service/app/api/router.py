from fastapi import APIRouter
from app.api.routes import email_reply, lead_scoring, sentiment, chatbot

api_router = APIRouter(prefix="/api/v1/ai")

api_router.include_router(email_reply.router)
api_router.include_router(lead_scoring.router)
api_router.include_router(sentiment.router)
api_router.include_router(chatbot.router)
