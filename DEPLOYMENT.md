# SkillSync — GCloud Deployment Guide

This document explains how the SkillSync backend is deployed to **Google Cloud Platform** using **GKE (Google Kubernetes Engine)** via a Jenkins CI/CD pipeline.

---

## Architecture Overview

```
Internet
   │
   ▼
skillsync.mooo.com  (NGINX Ingress + Let's Encrypt TLS)
   │
   ├─ /api/*         → api-gateway  (port 9090)
   ├─ /eureka/*      → eureka-server (port 8761)
   ├─ /rabbitmq/*    → RabbitMQ management UI (port 15672)
   ├─ /zipkin/*      → Zipkin (port 9411)
   ├─ /prometheus/*  → Prometheus (port 9090)
   ├─ /grafana/*     → Grafana (port 3000)
   └─ /sonarqube/*   → SonarQube (port 9000)

api-gateway (Spring Cloud Gateway)
   │  (routes via Eureka service discovery)
   ├─ auth-service
   ├─ user-service
   ├─ mentor-service
   ├─ skill-service
   ├─ session-service
   ├─ group-service
   ├─ review-service
   ├─ notification-service
   └─ payment-service

Infrastructure (all ClusterIP — internal only)
   ├─ MySQL 8.4         (StatefulSet, 10 Gi PVC)
   ├─ RabbitMQ 3        (StatefulSet)
   ├─ Redis 7           (Deployment)
   ├─ Zipkin            (distributed tracing)
   ├─ Prometheus        (metrics scraping)
   ├─ Grafana           (dashboards)
   └─ SonarQube + PostgreSQL  (code quality)
```

---

## GCP Resources

| Resource | Name / Value |
|---|---|
| GCP Project ID | `project-00909acc-d419-4f02-8a1` |
| Region | `asia-south1` |
| GKE Cluster | `skillsync-cluster` |
| Artifact Registry Repo | `skillsync-repo` |
| Image registry prefix | `asia-south1-docker.pkg.dev/<project-id>/skillsync-repo/` |
| Kubernetes Namespace | `skillsync` |
| Domain | `skillsync.mooo.com` |

---

## CI/CD Pipeline (Jenkinsfile)

The pipeline has **13 ordered stages**. Each is explained below.

### Stage 1 — Checkout
```
checkout scm
```
Pulls the latest code from source control.

---

### Stage 2 — Build & Test (parallel)
All 12 services are built in parallel using Maven.

- Infrastructure services (`config-server`, `eureka-server`, `api-gateway`, `payment-service`) skip tests:
  ```
  mvn clean package -Dmaven.test.skip=true
  ```
- Business services run full test suite:
  ```
  mvn clean verify
  ```

### Stage 2b — Publish Test Results
JUnit XML reports from `**/target/surefire-reports/*.xml` are published to Jenkins.

---

### Stage 3 — GCP Auth
```bash
gcloud config set project $PROJECT_ID
gcloud auth configure-docker asia-south1-docker.pkg.dev --quiet
```
Configures Docker to authenticate against Google Artifact Registry.

---

### Stage 4 — Docker Build & Push (parallel)
Each service is built into a Docker image tagged with the Jenkins build number and pushed to Artifact Registry:
```
asia-south1-docker.pkg.dev/<project-id>/skillsync-repo/<service-name>:<BUILD_NUMBER>
```
All 12 services are pushed in parallel.

---

### Stage 5 — Connect to GKE
```bash
gcloud container clusters get-credentials skillsync-cluster \
  --region asia-south1 --project <project-id>
```
Downloads GKE credentials into `kubectl` so subsequent stages can apply manifests.

---

### Stage 6 — Apply Base Configs
```bash
kubectl apply -f k8s/namespace.yaml    # creates 'skillsync' namespace
kubectl apply -f k8s/secrets.yaml      # MySQL password, JWT secret, OAuth keys, SMTP, Razorpay
kubectl apply -f k8s/configmap.yaml    # shared env vars (Eureka URL, Redis host, RabbitMQ host…)
```

**Secrets stored (base64-encoded):**
- `MYSQL_ROOT_PASSWORD`
- `RABBITMQ_USER` / `RABBITMQ_PASS`
- `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET`
- `RESEND_API_KEY`
- `JWT_SECRET`
- `SMTP_USERNAME` / `SMTP_PASSWORD`
- `GOOGLE_CLIENT_ID`

**ConfigMap shared env vars:**
```yaml
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
SPRING_CONFIG_IMPORT: optional:configserver:http://config-server:8888
SPRING_RABBITMQ_HOST: rabbitmq
SPRING_RABBITMQ_PORT: "5672"
SPRING_DATA_REDIS_HOST: redis
SPRING_DATA_REDIS_PORT: "6379"
JAVA_TOOL_OPTIONS: -Xms64m -Xmx192m
```

---

### Stage 7 — Deploy Infrastructure
Infrastructure components are applied first and the pipeline **waits** for MySQL, RabbitMQ, and Redis to be ready before proceeding:

| Component | Kind | Notes |
|---|---|---|
| MySQL 8.4 | StatefulSet | 10 Gi PVC; init SQL creates all 9 databases on first boot |
| RabbitMQ 3 | StatefulSet | Default user/pass from secrets |
| Redis 7 | Deployment | Used by user-service, mentor-service, skill-service for caching |
| Zipkin | Deployment | Distributed tracing UI |
| Prometheus | Deployment | Scrapes `/actuator/prometheus` on each service |
| Grafana | Deployment | Dashboard UI, reads from Prometheus |
| PostgreSQL | StatefulSet | Backing DB for SonarQube |
| SonarQube | Deployment | Static code analysis UI |

**MySQL databases created at init:**
```sql
CREATE DATABASE IF NOT EXISTS skillsync_auth;
CREATE DATABASE IF NOT EXISTS user_service;
CREATE DATABASE IF NOT EXISTS mentor_service;
CREATE DATABASE IF NOT EXISTS skill_service;
CREATE DATABASE IF NOT EXISTS session_service;
CREATE DATABASE IF NOT EXISTS `group-service`;
CREATE DATABASE IF NOT EXISTS `reviews-service`;
CREATE DATABASE IF NOT EXISTS `notification-service`;
CREATE DATABASE IF NOT EXISTS payment_service;
```

---

### Stage 8 — Deploy config-server
Deployed **first** and the pipeline waits for it to be `Ready` (timeout 300 s).  
All business services depend on config-server for centralized Spring Cloud Config properties.

---

### Stage 9 — Deploy eureka-server
Deployed **second** and the pipeline waits for it to be `Ready` (timeout 300 s).  
All business services register themselves with Eureka on startup.

---

### Stage 10 — Deploy Business Services (parallel)
Once config-server and eureka-server are healthy, all 9 business services are applied in parallel:

| Service | Port | Redis | RabbitMQ consumer |
|---|---|---|---|
| auth-service | — | No | Publishes `UserRegisteredEvent` |
| user-service | — | Yes | Consumes `UserRegisteredEvent`, `MentorApprovedEvent` |
| mentor-service | — | Yes | Publishes `MentorApprovedEvent` |
| skill-service | — | Yes | — |
| session-service | — | No | Publishes session events |
| group-service | — | No | — |
| review-service | — | No | Publishes `ReviewSubmittedEvent` |
| notification-service | — | No | Consumes all domain events |
| payment-service | — | No | Razorpay integration |

All services are `ClusterIP` — only reachable inside the cluster via the API gateway.

The image tag is substituted at deploy time:
```bash
sed 's|:latest|:<BUILD_NUMBER>|g' k8s/services/<service>.yaml | kubectl apply -f -
```

**Deployment spec for each service (example pattern):**
- `replicas: 1`
- `strategy: RollingUpdate` (maxSurge: 0, maxUnavailable: 1)
- `envFrom` → `skillsync-config` ConfigMap
- `env` → service-specific overrides + secrets references
- `readinessProbe` / `livenessProbe` on `/actuator/health`
- Resources: requests `250m CPU / 256Mi`, limits `500m CPU / 512Mi`

---

### Stage 11 — Deploy api-gateway
Deployed **last** and the pipeline waits for it to be `Ready` (timeout 300 s).  
Port 9090 is the single public entry point routed by Ingress.

---

### Stage 12 — Apply Ingress
Five ingress manifests are applied:

| File | Purpose |
|---|---|
| `ingress.yaml` | Main app — routes `/api/*` to api-gateway, `/eureka/*` to Eureka, `/rabbitmq/*` to RabbitMQ. TLS via cert-manager + Let's Encrypt. |
| `ingress-tools.yaml` | Observability tools — Zipkin, Prometheus, Grafana, SonarQube |
| `ingress-redirects.yaml` | HTTP → HTTPS redirects |
| `ingress-eureka-static.yaml` | Static routes for Eureka dashboard assets |
| `ingress-zipkin-api.yaml` | Zipkin API sub-path rules |

All routes sit on **`skillsync.mooo.com`** with NGINX Ingress Controller.  
TLS certificate is auto-provisioned by `cert-manager` using the `letsencrypt-prod` ClusterIssuer.

---

### Stage 13 — SonarQube Analysis
All 12 services are scanned in parallel using the Maven SonarQube plugin:
```bash
mvn sonar:sonar \
  -Dsonar.host.url=https://skillsync.mooo.com/sonarqube \
  -Dsonar.token=$SONAR_TOKEN \
  -Dsonar.projectKey=<service>
```
This stage uses `catchError(buildResult: 'SUCCESS')` — if SonarQube isn't ready on the first run the build still passes but the stage is marked `UNSTABLE`.

**One-time SonarQube setup:**
1. Open `https://skillsync.mooo.com/sonarqube`, login as `admin/admin`, change the password.
2. My Account → Security → Generate a **Global Analysis Token**.
3. Jenkins → Credentials → Add secret text with ID `sonar-token`.

---

## Post-pipeline

| Event | Action |
|---|---|
| SUCCESS | Prints build number; runs `kubectl get service api-gateway -n skillsync` to display the external IP |
| FAILURE | Prints failure message; check stage logs |

---

## Startup Order (Dependency Graph)

```
MySQL  ─────┐
RabbitMQ ───┼──► config-server ──► eureka-server ──► [all business services] ──► api-gateway
Redis ──────┘
```

This order is enforced both in `docker-compose.yml` (via `depends_on` + healthchecks) and in the Jenkinsfile (sequential stages 7 → 8 → 9 before stage 10).

---

## Local Development (Docker Compose)

For local runs without Kubernetes:

```bash
cd SkillSync-mine
docker compose up --build
```

Exposed ports locally:

| Service | Port |
|---|---|
| API Gateway | 9090 |
| Eureka | 8761 |
| Config Server | 8888 |
| RabbitMQ UI | 15672 |
| Zipkin | 9411 |
| Prometheus | 9091 |
| Grafana | 3000 |
| MySQL | 3306 |
| Redis | 6379 |

All business services have **no external ports** — access them only through the gateway on `:9090`.

---

## k8s Directory Layout

```
k8s/
├── namespace.yaml                # 'skillsync' namespace
├── secrets.yaml                  # all sensitive credentials
├── configmap.yaml                # shared Spring env vars
├── ingress.yaml                  # main app + Eureka/RabbitMQ routes (TLS)
├── ingress-tools.yaml            # Zipkin / Prometheus / Grafana / SonarQube
├── ingress-redirects.yaml        # HTTP → HTTPS
├── ingress-eureka-static.yaml    # Eureka static asset fix
├── ingress-zipkin-api.yaml       # Zipkin API sub-paths
├── infrastructure/
│   ├── mysql.yaml                # StatefulSet + PVC + init ConfigMap
│   ├── rabbitmq.yaml             # StatefulSet
│   ├── redis.yaml                # Deployment
│   ├── zipkin.yaml               # Deployment
│   ├── prometheus.yaml           # Deployment
│   ├── grafana.yaml              # Deployment
│   ├── postgres-sonarqube.yaml   # StatefulSet (SonarQube DB)
│   └── sonarqube.yaml            # Deployment
└── services/
    ├── config-server.yaml
    ├── eureka-server.yaml
    ├── api-gateway.yaml
    ├── auth-service.yaml
    ├── user-service.yaml
    ├── mentor-service.yaml
    ├── skill-service.yaml
    ├── session-service.yaml
    ├── group-service.yaml
    ├── review-service.yaml
    ├── notification-service.yaml
    └── payment-service.yaml
```
