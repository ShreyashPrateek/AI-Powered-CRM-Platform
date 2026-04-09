from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    # ── Server ────────────────────────────────────────────────────────────────
    APP_NAME: str = "CRM AI Service"
    APP_VERSION: str = "1.0.0"
    PORT: int = 8088
    LOG_LEVEL: str = "INFO"

    # ── HuggingFace ───────────────────────────────────────────────────────────
    # Email reply / chatbot generation model (instruction-tuned)
    # Use a smaller model by default so it runs without a GPU in dev.
    # Override with "meta-llama/Meta-Llama-3-8B-Instruct" or
    # "mistralai/Mistral-7B-Instruct-v0.3" when running on GPU hardware.
    GENERATION_MODEL: str = "microsoft/phi-2"
    GENERATION_MAX_NEW_TOKENS: int = 512
    GENERATION_TEMPERATURE: float = 0.7
    GENERATION_TOP_P: float = 0.9

    # Sentiment analysis — DistilBERT fine-tuned on SST-2
    SENTIMENT_MODEL: str = "distilbert-base-uncased-finetuned-sst-2-english"

    # Embeddings — used for RAG + lead scoring similarity
    EMBEDDING_MODEL: str = "sentence-transformers/all-MiniLM-L6-v2"

    # ── ChromaDB ──────────────────────────────────────────────────────────────
    CHROMA_HOST: str = "localhost"
    CHROMA_PORT: int = 8000
    CHROMA_COLLECTION_DEALS: str = "crm_deals"
    CHROMA_COLLECTION_LEADS: str = "crm_leads"

    # ── Model cache ───────────────────────────────────────────────────────────
    # Set to a persistent path in production (e.g. a mounted volume)
    HF_CACHE_DIR: str = "/tmp/hf_cache"

    # ── Device ────────────────────────────────────────────────────────────────
    # "auto" → use GPU if available, else CPU
    DEVICE: str = "auto"


@lru_cache
def get_settings() -> Settings:
    return Settings()
