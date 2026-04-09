"""
ChatbotService  (RAG — Retrieval-Augmented Generation)
───────────────────────────────────────────────────────
Flow:
  1. Embed the user's question.
  2. Query ChromaDB for the top-k most relevant deal/lead documents.
  3. Build a grounded prompt from the retrieved context.
  4. Generate an answer with the generation model.
  5. Return the answer + the source documents used.

Ingestion helpers (upsert_deals / upsert_leads) are called by the
/ingest endpoints so the Spring services can push data into the vector DB.
"""
import logging
from typing import Any

from app.models.registry import registry
from app.models.vector_store import get_vector_store
from app.schemas.chatbot import (
    ChatRequest,
    ChatResponse,
    IngestDealsRequest,
    IngestLeadsRequest,
    SourceDocument,
)

logger = logging.getLogger(__name__)

_TOP_K = 5
_MAX_CONTEXT_CHARS = 3000

_SYSTEM_PROMPT = """\
You are an intelligent CRM assistant with access to real-time deal and lead data.
Answer the user's question using ONLY the provided context.
If the context does not contain enough information, say so honestly.
Be concise, factual, and helpful.
"""

_RAG_PROMPT_TEMPLATE = """\
<|system|>
{system}
</s>
<|user|>
Context from CRM database:
{context}

Question: {question}
</s>
<|assistant|>
"""


class ChatbotService:

    # ── Query ─────────────────────────────────────────────────────────────────

    def chat(self, req: ChatRequest, tenant_id: str = "default") -> ChatResponse:
        question_emb = registry.embedding_model.encode(
            [req.question], normalize_embeddings=True
        ).tolist()[0]

        # Retrieve from both collections scoped to this tenant
        deal_docs = self._query_collection("deals", question_emb, tenant_id)
        lead_docs = self._query_collection("leads", question_emb, tenant_id)
        all_docs = (deal_docs + lead_docs)[:_TOP_K]

        context = self._build_context(all_docs)
        prompt = _RAG_PROMPT_TEMPLATE.format(
            system=_SYSTEM_PROMPT,
            context=context,
            question=req.question,
        )

        raw: list[dict] = registry.generation_pipeline(prompt)
        full_text: str = raw[0]["generated_text"]
        answer = self._extract_answer(full_text, prompt)

        logger.info(
            "Chatbot answered | question_len=%d | sources=%d | answer_len=%d",
            len(req.question), len(all_docs), len(answer),
        )

        return ChatResponse(
            answer=answer,
            sources=all_docs,
            model_used=registry.generation_pipeline.model.config.name_or_path,
        )

    # ── Ingestion ─────────────────────────────────────────────────────────────

    def upsert_deals(self, req: IngestDealsRequest, tenant_id: str = "default") -> dict[str, int]:
        store = get_vector_store()
        collection = store.deals_collection()

        documents, embeddings, metadatas, ids = [], [], [], []

        for deal in req.deals:
            # Validate id is present and is a safe scalar
            deal_id = str(deal.get("id", "")).strip()
            if not deal_id:
                continue
            text = self._deal_to_text(deal)
            emb = registry.embedding_model.encode([text], normalize_embeddings=True).tolist()[0]
            documents.append(text)
            embeddings.append(emb)
            # Tenant tag on every record — prevents cross-tenant retrieval
            metadatas.append({**{k: str(v) for k, v in deal.items()}, "tenant_id": tenant_id})
            ids.append(f"{tenant_id}_deal_{deal_id}")

        if not documents:
            return {"upserted": 0}

        collection.upsert(documents=documents, embeddings=embeddings, metadatas=metadatas, ids=ids)
        logger.info("Upserted %d deals into ChromaDB | tenant=%s", len(documents), tenant_id)
        return {"upserted": len(documents)}

    def upsert_leads(self, req: IngestLeadsRequest, tenant_id: str = "default") -> dict[str, int]:
        store = get_vector_store()
        collection = store.leads_collection()

        documents, embeddings, metadatas, ids = [], [], [], []

        for lead in req.leads:
            lead_id = str(lead.get("id", "")).strip()
            if not lead_id:
                continue
            text = self._lead_to_text(lead)
            emb = registry.embedding_model.encode([text], normalize_embeddings=True).tolist()[0]
            documents.append(text)
            embeddings.append(emb)
            metadatas.append({**{k: str(v) for k, v in lead.items()}, "tenant_id": tenant_id})
            ids.append(f"{tenant_id}_lead_{lead_id}")

        if not documents:
            return {"upserted": 0}

        collection.upsert(documents=documents, embeddings=embeddings, metadatas=metadatas, ids=ids)
        logger.info("Upserted %d leads into ChromaDB | tenant=%s", len(documents), tenant_id)
        return {"upserted": len(documents)}

    # ── Private helpers ───────────────────────────────────────────────────────

    def _query_collection(self, kind: str, embedding: list[float], tenant_id: str = "default") -> list[SourceDocument]:
        store = get_vector_store()
        collection = store.deals_collection() if kind == "deals" else store.leads_collection()

        try:
            results = collection.query(
                query_embeddings=[embedding],
                n_results=_TOP_K,
                where={"tenant_id": tenant_id},   # tenant isolation — prevents cross-tenant leaks
                include=["documents", "metadatas", "distances"],
            )
        except Exception as exc:
            logger.warning("ChromaDB query failed for %s: %s", kind, exc)
            return []

        docs = []
        for doc, meta, dist in zip(
            results["documents"][0],
            results["metadatas"][0],
            results["distances"][0],
        ):
            docs.append(SourceDocument(
                content=doc,
                metadata=meta,
                relevance_score=round(1 - dist, 4),  # cosine distance → similarity
                source=kind,
            ))
        return docs

    def _build_context(self, docs: list[SourceDocument]) -> str:
        parts = []
        total = 0
        for i, doc in enumerate(docs, 1):
            entry = f"[{i}] ({doc.source.upper()}) {doc.content}"
            if total + len(entry) > _MAX_CONTEXT_CHARS:
                break
            parts.append(entry)
            total += len(entry)
        return "\n".join(parts) if parts else "No relevant CRM data found."

    @staticmethod
    def _extract_answer(full_text: str, prompt: str) -> str:
        if full_text.startswith(prompt):
            return full_text[len(prompt):].strip()
        parts = full_text.split("<|assistant|>")
        return parts[-1].strip() if len(parts) > 1 else full_text.strip()

    @staticmethod
    def _deal_to_text(deal: dict[str, Any]) -> str:
        return (
            f"Deal '{deal.get('title', 'Unknown')}' "
            f"worth ${deal.get('value', 0)} "
            f"in stage {deal.get('stage', 'UNKNOWN')} "
            f"with {deal.get('probability', 0)}% probability, "
            f"expected close {deal.get('expectedCloseDate', 'unknown')}. "
            f"Notes: {deal.get('notes', 'none')}."
        )

    @staticmethod
    def _lead_to_text(lead: dict[str, Any]) -> str:
        return (
            f"Lead '{lead.get('name', 'Unknown')}' "
            f"from {lead.get('company', 'unknown company')} "
            f"in {lead.get('industry', 'unknown')} industry, "
            f"status {lead.get('status', 'UNKNOWN')}, "
            f"source {lead.get('source', 'unknown')}. "
            f"Notes: {lead.get('notes', 'none')}."
        )


chatbot_service = ChatbotService()
