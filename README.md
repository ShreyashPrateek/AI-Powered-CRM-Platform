# AI-Powered-CRM-Platform

## Project Goal
Build a production-grade AI CRM system that allows companies to:
1. Manage leads
2. Track deals
3. Automate sales
4. Generate AI insights
5. Predict sales outcomes
6. Analyze customer sentiment

---

## High-Level Architecture

```
Frontend (Vue / React)
        │
API Gateway (Spring Cloud)
        │
────────────────────────────────
Microservices Layer
────────────────────────────────
  - Auth Service
  - User Service
  - Lead Service
  - Deal / Pipeline Service
  - Email Automation Service
  - Notification Service
  - Analytics Service
  - AI Service
        │
Event Streaming (Kafka)
        │
────────────────────────────────
Data Layer
────────────────────────────────
  - PostgreSQL  (Primary DB)
  - Redis       (Caching)
  - Elasticsearch (Search)
  - ChromaDB    (Vector DB)
```

---

## Tech Stack

### Backend
| Layer        | Technology                                      |
|--------------|-------------------------------------------------|
| Language     | Java 17+                                        |
| Framework    | Spring Boot, Spring Security, Spring Cloud      |
| ORM          | Hibernate, Spring Data JPA                      |

### Databases
| Role         | Technology     |
|--------------|----------------|
| Primary DB   | PostgreSQL      |
| Caching      | Redis           |
| Search       | Elasticsearch   |
| Vector DB    | ChromaDB        |

### Messaging
| Role             | Technology    |
|------------------|---------------|
| Event Streaming  | Apache Kafka  |

### AI Stack
| Role             | Technology                                          |
|------------------|-----------------------------------------------------|
| Models           | Llama 3, Mistral 7B, DistilBERT (Hugging Face)      |
| Libraries        | Transformers, LangChain, Sentence Transformers      |

### DevOps
| Role             | Technology                          |
|------------------|-------------------------------------|
| Containerization | Docker, Docker Compose              |
| Orchestration    | Kubernetes                          |
| CI/CD            | GitHub Actions                      |

### Monitoring
| Role             | Technology                  |
|------------------|-----------------------------|
| Metrics          | Prometheus, Grafana         |
| Logging          | ELK Stack                   |

---

## Project Structure

```
├── frontend/                  # Vue / React frontend
├── api-gateway/               # Spring Cloud API Gateway
├── services/
│   ├── auth-service/
│   ├── user-service/
│   ├── lead-service/
│   ├── deal-service/
│   ├── email-service/
│   ├── notification-service/
│   ├── analytics-service/
│   └── ai-service/
├── infrastructure/
│   ├── kafka/
│   ├── postgres/
│   ├── redis/
│   ├── elasticsearch/
│   └── chromadb/
├── monitoring/
│   ├── prometheus/
│   └── grafana/
├── k8s/                       # Kubernetes manifests
├── .github/workflows/         # GitHub Actions CI/CD
└── docs/
```

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Python 3.10+ (for AI service)

### Run Infrastructure Locally
```bash
docker-compose up -d
```
