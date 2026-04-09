from fastapi import APIRouter
from app.schemas.chatbot import (
    ChatRequest,
    ChatResponse,
    IngestDealsRequest,
    IngestLeadsRequest,
    IngestResponse,
)
from app.services.chatbot_service import chatbot_service

router = APIRouter(prefix="/chat", tags=["CRM Chatbot"])


@router.post("", response_model=ChatResponse, summary="Ask the CRM AI assistant")
def chat(req: ChatRequest) -> ChatResponse:
    """
    Ask a natural language question about your CRM data.

    Uses RAG: retrieves relevant deals/leads from ChromaDB, then generates
    a grounded answer using the generation model.

    Example questions:
    - "Which deals are likely to close this week?"
    - "Show me high-priority leads in the technology sector"
    - "What is the status of deals in the negotiation stage?"
    """
    return chatbot_service.chat(req)


@router.post("/ingest/deals", response_model=IngestResponse, summary="Ingest deals into vector DB")
def ingest_deals(req: IngestDealsRequest) -> IngestResponse:
    """
    Upsert deal records into ChromaDB for RAG retrieval.
    Called by the Spring deal-service after create/update events.
    """
    result = chatbot_service.upsert_deals(req)
    return IngestResponse(**result)


@router.post("/ingest/leads", response_model=IngestResponse, summary="Ingest leads into vector DB")
def ingest_leads(req: IngestLeadsRequest) -> IngestResponse:
    """
    Upsert lead records into ChromaDB for RAG retrieval.
    Called by the Spring lead-service after create/update events.
    """
    result = chatbot_service.upsert_leads(req)
    return IngestResponse(**result)
