import logging
import sys
from app.core.config import get_settings


def setup_logging() -> None:
    settings = get_settings()
    logging.basicConfig(
        level=settings.LOG_LEVEL.upper(),
        format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
        datefmt="%Y-%m-%dT%H:%M:%S",
        stream=sys.stdout,
    )
    # Silence noisy third-party loggers
    for noisy in ("transformers", "sentence_transformers", "chromadb", "httpx"):
        logging.getLogger(noisy).setLevel(logging.WARNING)
