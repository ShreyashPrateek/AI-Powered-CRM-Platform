from fastapi import APIRouter
from app.schemas.email_reply import EmailReplyRequest, EmailReplyResponse
from app.services.email_reply_service import email_reply_service

router = APIRouter(prefix="/generate-email", tags=["Email Reply"])


@router.post("", response_model=EmailReplyResponse, summary="Generate AI email reply")
def generate_email_reply(req: EmailReplyRequest) -> EmailReplyResponse:
    """
    Generate a professional CRM email reply using Llama 3 / Mistral.

    - **context**: The customer's email or interaction summary
    - **tone**: professional | friendly | assertive | empathetic
    """
    return email_reply_service.generate(req)
