"""
ChromaDB client — singleton wrapper.
Collections are created lazily on first access.
"""
import logging
from functools import lru_cache

import chromadb
from chromadb import Collection

from app.core.config import get_settings

logger = logging.getLogger(__name__)


class VectorStore:
    def __init__(self) -> None:
        settings = get_settings()
        self._client = chromadb.HttpClient(
            host=settings.CHROMA_HOST,
            port=settings.CHROMA_PORT,
        )
        self._settings = settings
        logger.info(
            "ChromaDB client connected → %s:%s",
            settings.CHROMA_HOST,
            settings.CHROMA_PORT,
        )

    def deals_collection(self) -> Collection:
        return self._client.get_or_create_collection(
            name=self._settings.CHROMA_COLLECTION_DEALS,
            metadata={"hnsw:space": "cosine"},
        )

    def leads_collection(self) -> Collection:
        return self._client.get_or_create_collection(
            name=self._settings.CHROMA_COLLECTION_LEADS,
            metadata={"hnsw:space": "cosine"},
        )


@lru_cache
def get_vector_store() -> VectorStore:
    return VectorStore()
