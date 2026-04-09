"""
EmailReplyService
─────────────────
Generates a professional CRM email reply using the loaded generation model.

The prompt is structured as an instruction template compatible with both
Llama-3-Instruct and Mistral-Instruct chat formats.
"""
import logging
import re

from app.models.registry import registry
from app.schemas.email_reply import EmailReplyRequest, EmailReplyResponse

_LOG_SANITIZE = re.compile(r"[\r\n\t]")


def _safe(value: str) -> str:
    """Strip newlines/tabs from user input before logging to prevent log injection."""
    return _LOG_SANITIZE.sub(" ", str(value))

logger = logging.getLogger(__name__)

_TONE_GUIDANCE = {
    "professional": "formal, concise, and business-appropriate",
    "friendly":     "warm, approachable, and conversational",
    "assertive":    "confident, direct, and action-oriented",
    "empathetic":   "understanding, supportive, and solution-focused",
}

_PROMPT_TEMPLATE = """\
<|system|>
You are an expert B2B sales representative writing a CRM email reply.
Write ONLY the email body — no subject line, no metadata, no commentary.
Tone: {tone_guidance}.
</s>
<|user|>
Customer context:
{context}

Recipient name: {recipient_name}

Write a professional email reply body.
</s>
<|assistant|>
"""


class EmailReplyService:
    def generate(self, req: EmailReplyRequest) -> EmailReplyResponse:
        tone = req.tone.lower() if req.tone else "professional"
        tone_guidance = _TONE_GUIDANCE.get(tone, _TONE_GUIDANCE["professional"])

        prompt = _PROMPT_TEMPLATE.format(
            tone_guidance=tone_guidance,
            context=req.context.strip(),
            recipient_name=req.recipient_name or "there",
        )

        logger.debug("Generating email reply | tone=%s | recipient=%s", _safe(tone), _safe(req.recipient_email))

        raw: list[dict] = registry.generation_pipeline(prompt)
        generated_text: str = raw[0]["generated_text"]

        # Strip the prompt prefix — keep only the assistant's reply
        body = self._extract_reply(generated_text, prompt)

        logger.info("Email reply generated | recipient=%s | chars=%d", _safe(req.recipient_email), len(body))

        return EmailReplyResponse(
            recipient_email=req.recipient_email,
            subject=self._build_subject(req.context),
            body=body,
            model_used=registry.generation_pipeline.model.config.name_or_path,
        )

    # ── Helpers ───────────────────────────────────────────────────────────────

    def _extract_reply(self, full_text: str, prompt: str) -> str:
        """Remove the prompt prefix from the generated output."""
        if full_text.startswith(prompt):
            reply = full_text[len(prompt):].strip()
        else:
            # Fallback: take everything after the last <|assistant|> tag
            parts = full_text.split("<|assistant|>")
            reply = parts[-1].strip()

        # Remove any trailing special tokens
        reply = re.sub(r"<\|.*?\|>", "", reply).strip()
        return reply or full_text.strip()

    def _build_subject(self, context: str) -> str:
        """Derive a short subject line from the context."""
        first_sentence = context.split(".")[0].strip()
        subject = first_sentence[:60] if len(first_sentence) > 60 else first_sentence
        return f"Re: {subject}" if subject else "Following up on your enquiry"


email_reply_service = EmailReplyService()
