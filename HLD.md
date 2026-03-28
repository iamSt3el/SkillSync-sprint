# SkillSync — High-Level Design

```mermaid
graph TB

    classDef client  fill:#e0f2fe,stroke:#0284c7,color:#0c4a6e
    classDef infra   fill:#ede9fe,stroke:#7c3aed,color:#2e1065
    classDef service fill:#dbeafe,stroke:#3b82f6,color:#1e3a5f
    classDef queue   fill:#fef9c3,stroke:#ca8a04,color:#713f12
    classDef db      fill:#f1f5f9,stroke:#94a3b8,color:#334155
    classDef obs     fill:#f0fdf4,stroke:#16a34a,color:#14532d

    CLIENT(["Client\nWeb / Mobile"]):::client

    subgraph INFRA["Infrastructure"]
        direction LR
        GW["API Gateway :9090\nJWT Auth · Routing"]:::infra
        EUR["Eureka :8761\nService Discovery"]:::infra
        CFG["Config Server :8888\nCentralised Config"]:::infra
    end

    subgraph SVC["Microservices  —  each registers with Eureka and fetches config from Config Server"]
        direction LR
        AUTH["Auth\n:8081"]:::service
        USER["User\n:8082"]:::service
        MENTOR["Mentor\n:8083"]:::service
        SKILL["Skill\n:8084"]:::service
        GROUP["Group\n:8086"]:::service
        SESSION["Session\n:8085"]:::service
        REVIEW["Review\n:8087"]:::service
        NOTIF["Notif\n:8088"]:::service
    end

    MQ(["RabbitMQ :5672\nAsync Event Bus"]):::queue

    subgraph DBS["MySQL  —  one dedicated schema per service"]
        direction LR
        DB_A[("auth")]:::db
        DB_U[("user")]:::db
        DB_M[("mentor")]:::db
        DB_S[("skill")]:::db
        DB_G[("group")]:::db
        DB_SE[("session")]:::db
        DB_R[("review")]:::db
        DB_N[("notif")]:::db
    end

    subgraph OBS["Observability"]
        direction LR
        PROM["Prometheus\nMetrics"]:::obs
        ZIP["Zipkin\nTracing"]:::obs
        GRAF["Grafana\nDashboards"]:::obs
    end

    %% ── Request Routing ───────────────────────────────────────────
    CLIENT -->|HTTPS| GW
    GW -.->|discover| EUR
    GW --> AUTH & USER & MENTOR & SKILL & GROUP & SESSION & REVIEW & NOTIF

    %% ── Synchronous Feign Calls ───────────────────────────────────
    MENTOR -->|skill lookup| SKILL
    MENTOR -->|user lookup| USER
    SESSION -->|mentor exists?| MENTOR
    NOTIF -->|user lookup| USER

    %% ── Async Events — Producers ──────────────────────────────────
    AUTH    -- user.registered --> MQ
    MENTOR  -- mentor.approved --> MQ
    REVIEW  -- review.submitted --> MQ
    SESSION -- session.* --> MQ
    GROUP   -- group.member.* --> MQ

    %% ── Async Events — Consumers ──────────────────────────────────
    MQ -- user.registered --> USER
    MQ -- mentor.approved --> AUTH & USER
    MQ -- review.submitted --> MENTOR
    MQ -- all events --> NOTIF

    %% ── Persistence ───────────────────────────────────────────────
    AUTH --- DB_A
    USER --- DB_U
    MENTOR --- DB_M
    SKILL --- DB_S
    GROUP --- DB_G
    SESSION --- DB_SE
    REVIEW --- DB_R
    NOTIF --- DB_N

    %% ── Observability ─────────────────────────────────────────────
    AUTH & USER & MENTOR & SKILL & GROUP & SESSION & REVIEW & NOTIF -->|metrics| PROM
    AUTH & USER & MENTOR & SESSION -.->|traces| ZIP
    PROM --> GRAF
```

---

## Component Summary

| Component | Port | Role |
|---|---|---|
| **API Gateway** | 9090 | Single entry point — JWT validation, routing, load balancing |
| **Eureka Server** | 8761 | Service registry and client-side discovery |
| **Config Server** | 8888 | Externalised configuration for all services |
| **Auth Service** | 8081 | User registration, login, JWT issuance and refresh |
| **User Service** | 8082 | User profile management, admin operations |
| **Mentor Service** | 8083 | Mentor application, discovery, availability, rating |
| **Skill Service** | 8084 | Skill catalog — read-only for users, CRUD for admins |
| **Group Service** | 8086 | Study group creation, membership management |
| **Session Service** | 8085 | Mentoring session booking and lifecycle (requested → completed) |
| **Review Service** | 8087 | Post-session ratings and feedback submission |
| **Notification Service** | 8088 | Email and in-app notifications triggered by domain events |
| **RabbitMQ** | 5672 | Async event bus decoupling all producers from consumers |
| **MySQL** | — | One dedicated schema per service (database-per-service pattern) |
| **Prometheus** | — | Metrics scraping via `/actuator/prometheus` on all services |
| **Zipkin** | — | Distributed trace collection via Micrometer Brave bridge |
| **Grafana** | — | Unified observability dashboards sourced from Prometheus |

---

## Key Design Patterns

- **API Gateway Pattern** — all external traffic enters through a single gateway with JWT authentication and route-level filtering
- **Database per Service** — each microservice owns its schema; no cross-service DB joins
- **Event-Driven Architecture** — domain events over RabbitMQ decouple services and drive side effects (notifications, role updates, rating recalculations)
- **Synchronous Feign Clients** — used only for mandatory, real-time validation (mentor existence check, user/skill lookups)
- **Centralised Configuration** — all service properties externalised to Config Server backed by Git
- **Service Discovery** — Eureka enables location-transparent load-balanced calls between services
- **Observability** — Prometheus + Grafana for metrics, Zipkin for distributed tracing across service boundaries
