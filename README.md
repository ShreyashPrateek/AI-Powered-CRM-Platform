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
└── docs/
```
