"""
ModelRegistry
─────────────
Loads all HuggingFace models once at startup.
Every service imports from here — no model is loaded more than once.
"""
import logging
from dataclasses import dataclass, field
from typing import Optional

import torch
from transformers import (
    AutoTokenizer,
    AutoModelForCausalLM,
    pipeline,
    Pipeline,
)
from sentence_transformers import SentenceTransformer

from app.core.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


def _resolve_device() -> str:
    if settings.DEVICE == "auto":
        return "cuda" if torch.cuda.is_available() else "cpu"
    return settings.DEVICE


@dataclass
class ModelRegistry:
    # Text generation (email reply + chatbot)
    generation_pipeline: Optional[Pipeline] = field(default=None, init=False)

    # Sentiment analysis (DistilBERT)
    sentiment_pipeline: Optional[Pipeline] = field(default=None, init=False)

    # Sentence embeddings (RAG + lead scoring)
    embedding_model: Optional[SentenceTransformer] = field(default=None, init=False)

    _loaded: bool = field(default=False, init=False)

    def load(self) -> None:
        if self._loaded:
            return

        device = _resolve_device()
        logger.info("Loading AI models on device=%s", device)

        # ── 1. Generation model ───────────────────────────────────────────────
        logger.info("Loading generation model: %s", settings.GENERATION_MODEL)
        tokenizer = AutoTokenizer.from_pretrained(
            settings.GENERATION_MODEL,
            cache_dir=settings.HF_CACHE_DIR,
        )
        gen_model = AutoModelForCausalLM.from_pretrained(
            settings.GENERATION_MODEL,
            cache_dir=settings.HF_CACHE_DIR,
            torch_dtype=torch.float16 if device == "cuda" else torch.float32,
            device_map=device if device == "cuda" else None,
        )
        self.generation_pipeline = pipeline(
            "text-generation",
            model=gen_model,
            tokenizer=tokenizer,
            device=0 if device == "cuda" else -1,
            max_new_tokens=settings.GENERATION_MAX_NEW_TOKENS,
            temperature=settings.GENERATION_TEMPERATURE,
            top_p=settings.GENERATION_TOP_P,
            do_sample=True,
            pad_token_id=tokenizer.eos_token_id,
        )
        logger.info("Generation model loaded ✓")

        # ── 2. Sentiment model ────────────────────────────────────────────────
        logger.info("Loading sentiment model: %s", settings.SENTIMENT_MODEL)
        self.sentiment_pipeline = pipeline(
            "text-classification",
            model=settings.SENTIMENT_MODEL,
            cache_dir=settings.HF_CACHE_DIR,
            device=0 if device == "cuda" else -1,
            truncation=True,
            max_length=512,
        )
        logger.info("Sentiment model loaded ✓")

        # ── 3. Embedding model ────────────────────────────────────────────────
        logger.info("Loading embedding model: %s", settings.EMBEDDING_MODEL)
        self.embedding_model = SentenceTransformer(
            settings.EMBEDDING_MODEL,
            cache_folder=settings.HF_CACHE_DIR,
            device=device,
        )
        logger.info("Embedding model loaded ✓")

        self._loaded = True
        logger.info("All AI models ready ✓")


# ── Module-level singleton ────────────────────────────────────────────────────
registry = ModelRegistry()
