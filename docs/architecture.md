# CRM Platform — Architecture & Developer Guide

## Running Locally

```bash
# 1. Start all infrastructure + services
docker-compose up -d

# 2. Frontend dev server
cd frontend && npm install && npm run dev
# → http://localhost:5173

# 3. API Gateway
# → http://localhost:8080

# 4. Grafana dashboards
# → http://localhost:3001  (admin / admin)

# 5. Prometheus
# → http://localhost:9090
```

## Service Ports

| Service              | Port |
|----------------------|------|
| API Gateway          | 8080 |
| Auth Service         | 8081 |
| User Service         | 8082 |
| Lead Service         | 8083 |
| Deal Service         | 8084 |
| Email Service        | 8085 |
| Notification Service | 8086 |
| Analytics Service    | 8087 |
| AI Service           | 8088 |
| PostgreSQL           | 5432 |
| Redis                | 6379 |
| Kafka                | 9092 |
| Elasticsearch        | 9200 |
| Kibana               | 5601 |
| ChromaDB             | 8000 |
| Prometheus           | 9090 |
| Grafana              | 3001 |

## Kubernetes Deployment

```bash
# Apply all manifests in order
kubectl apply -f k8s/00-namespace-config.yml
kubectl apply -f k8s/01-infrastructure.yml
kubectl apply -f k8s/02-services.yml
kubectl apply -f k8s/03-hpa-ingress.yml

# Update image references in k8s/02-services.yml
# Replace YOUR_ORG with your GitHub org before applying.
```

## Environment Variables

Copy `.env.example` in `services/ai-service/` and set:
- `GENERATION_MODEL` — HuggingFace model ID (default: `microsoft/phi-2`)
- `JWT_SECRET` — must be ≥ 32 chars, same value across all services
- `SMTP_*` — SMTP credentials for email sending
- `TWILIO_*` — Twilio credentials for SMS notifications

## CI/CD

- `.github/workflows/ci.yml` — runs on every push/PR: builds all Java services, lints Python, type-checks + builds frontend
- `.github/workflows/cd.yml` — runs on push to `main`: builds Docker images → pushes to GHCR → deploys to Kubernetes

Set these GitHub secrets for CD:
- `KUBECONFIG` — base64-encoded kubeconfig for your cluster

## AI Models

The AI service loads models from HuggingFace on startup:

| Feature           | Model                                          |
|-------------------|------------------------------------------------|
| Email reply       | `microsoft/phi-2` (override with Llama-3/Mistral) |
| Sentiment         | `distilbert-base-uncased-finetuned-sst-2-english` |
| Embeddings (RAG)  | `sentence-transformers/all-MiniLM-L6-v2`       |

For GPU inference, set `DEVICE=cuda` and use a larger generation model.
