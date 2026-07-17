# CX Broker — Dynamic Ingestion Service

> An event-driven, asynchronous data ingestion platform built for high-throughput batch processing. Accepts massive payloads without blocking, fans work out through a message broker, and streams real-time progress back to the dashboard via WebSockets.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [System Architecture](#️-system-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Reference](#-api-reference)
- [WebSocket Events](#-websocket-events)
- [Hexagonal Architecture](#-hexagonal-architecture)
- [Data Flow](#-data-flow)
- [Development Roadmap](#-development-roadmap)

---

## 🎯 Overview

CX Broker solves the classic **thundering herd problem** for customer experience data pipelines. When a user uploads thousands of customer tickets at once, a naive synchronous API would time out or collapse under load. CX Broker instead:

1. **Accepts** the batch upload via `POST /api/jobs` instantly.
2. **Returns** `202 Accepted` immediately — no waiting.
3. **Enqueues** the entire payload to RabbitMQ for background processing.
4. **Processes** each ticket asynchronously (e.g., AI sentiment classification).
5. **Pushes** live status updates to the Angular dashboard via WebSocket.

This makes it suitable as a backend for any heavy async workload: AI inference pipelines, document parsing, bulk email sends, or ETL jobs.

---

## 🏗️ System Architecture

```mermaid
flowchart TD
    classDef frontend fill:#dd0031,stroke:#c3002f,stroke-width:2px,color:#fff
    classDef backend fill:#6db33f,stroke:#5fa134,stroke-width:2px,color:#fff
    classDef broker fill:#ff6600,stroke:#e55c00,stroke-width:2px,color:#fff
    classDef database fill:#336791,stroke:#2b5577,stroke-width:2px,color:#fff
    classDef user fill:#f9f9f9,stroke:#333,stroke-width:2px

    User((User)):::user

    subgraph Frontend ["Client Workspace (Angular 17+)"]
        UI["Real-Time Dashboard"]:::frontend
    end

    subgraph Backend ["Server Workspace (Java 21 / Spring Boot 4.1)"]  
        API["Ingestion REST API\n(POST /api/jobs → 202)"]:::backend
        Worker["Async Worker\n(RabbitMQ Consumer)"]:::backend
        WS["WebSocket Service\n(STOMP over SockJS)"]:::backend
    end

    subgraph Infrastructure ["Docker Compose"]
        MQ[("RabbitMQ\n(Message Broker)")]:::broker
        DB[("PostgreSQL\n(JSONB Storage)")]:::database
    end

    User -- "Uploads Batch CSV / JSON" --> UI
    UI -- "POST /api/jobs" --> API

    API -- "1. Save job as PENDING" --> DB
    API -- "2. Publish JobEvent" --> MQ
    API -- "3. Return 202 Accepted" --> UI

    MQ -- "Consume JobEvent" --> Worker
    Worker -- "Update job to COMPLETED" --> DB
    Worker -- "Emit progress event" --> WS
    WS -- "Stream real-time metrics" --> UI
```

---

## 🛠️ Tech Stack

| Layer | Technology | Version | Role |
| :--- | :--- | :--- | :--- |
| **Frontend** | Angular | 17+ | Real-time WebSocket dashboard |
| **Backend** | Java + Spring Boot | 21 / 4.1.0 | REST API, async worker, WebSocket server |
| **ORM** | Spring Data JPA + Hibernate | — | Database access layer |
| **Message Broker** | RabbitMQ | — | Async task queue (AMQP) |
| **Database** | PostgreSQL | — | Job state + JSONB payload storage |
| **Build Tool** | Maven | — | Dependency management & packaging |
| **Boilerplate** | Lombok | — | Reduces DTO/entity verbosity |
| **Architecture** | Hexagonal (Ports & Adapters) | — | Domain isolation from frameworks |

---

## 📁 Project Structure

The backend follows a **package-by-feature hexagonal** layout. Each feature is its own self-contained hexagon — domain, application logic, and infrastructure adapters all live inside the feature package. This keeps features independently evolvable and prevents cross-feature coupling.

```
java-spring-rabbitmq-broker/               ← Monorepo root
├── readme.md
├── client/                                ← Angular 17+ frontend
│   └── src/
│       ├── app/
│       │   ├── core/                      # Singleton services, guards, interceptors
│       │   ├── shared/                    # Reusable standalone components
│       │   └── features/
│       │       ├── dashboard/             # Real-time job dashboard
│       │       └── ingestion/             # Upload UI
│       └── environments/
└── server/                                ← Spring Boot backend
    ├── pom.xml
    └── src/
        └── main/
            ├── java/com/example/cx_broker/
            │   ├── CxBrokerApplication.java          ← Entry point
            │   │
            │   └── ticket_ingestion/                 ← Feature: Ticket Ingestion
            │       │
            │       ├── domain/                       ← Pure Java. Zero framework imports.
            │       │   ├── model/
            │       │   │   ├── TicketBatch.java       # Aggregate root
            │       │   │   ├── SupportTicket.java     # Entity
            │       │   │   └── BatchStatus.java       # Enum: PENDING → PROCESSING → COMPLETED | FAILED
            │       │   └── ports/
            │       │       ├── in/                   # Driving ports (use-case interfaces)
            │       │       │   └── IngestBatchUseCase.java
            │       │       └── out/                  # Driven ports (infrastructure contracts)
            │       │           ├── BatchRepositoryPort.java
            │       │           └── MessagePublisherPort.java
            │       │
            │       ├── application/                  ← Use-case implementations (orchestrators)
            │       │   └── service/
            │       │       ├── BatchIngestionService.java   # Implements IngestBatchUseCase
            │       │       └── BatchProcessingService.java  # Async consumer handler
            │       │
            │       └── infrastructure/               ← Spring Boot adapters
            │           ├── web/                      # REST controller (POST /api/jobs)
            │           ├── messaging/                # RabbitMQ publisher & @RabbitListener
            │           ├── persistence/              # JPA entities & Spring Data repositories
            │           └── websocket/                # STOMP config & progress broadcaster
            └── resources/
                └── application.properties
```

> **Architecture Rule:** The `ticket_ingestion/domain/` package must **never** import anything from `org.springframework`, `jakarta.persistence`, or `com.rabbitmq`. It is pure Java — testable without any container.

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** — [Adoptium](https://adoptium.net/)
- **Maven 3.9+** — bundled via `./mvnw`
- **Node.js 20+** — for the Angular client
- **Docker & Docker Compose** *(deferred — see roadmap Phase 7)* — for PostgreSQL and RabbitMQ

> ⏳ **Infrastructure Setup Deferred**
> Docker Compose (PostgreSQL + RabbitMQ) is planned but not required for the current development phase. The domain model and application layer can be built and unit-tested without any running infrastructure. See [Phase 7 in the roadmap](#️-development-roadmap).

### 1. Run the Backend

Once infrastructure is available:

```bash
cd server
./mvnw spring-boot:run
```

API available at: `http://localhost:8080`

### 2. Run the Frontend

```bash
cd client
npm install
ng serve
```

Dashboard available at: `http://localhost:4200`

---

## ⚙️ Configuration

All backend configuration lives in [`server/src/main/resources/application.properties`](server/src/main/resources/application.properties).

```properties
# Server
server.port=8080

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/cx_broker_db
spring.datasource.username=admin
spring.datasource.password=admin
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate — auto-creates/updates tables from JPA entities
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=adminpassword
```

> ⚠️ **Security:** Move credentials to environment variables or a secrets manager before any production deployment.

---

## 📡 API Reference

### `POST /api/jobs`

Accepts a batch of items for asynchronous processing.

**Request Body**

```json
{
  "source": "zendesk",
  "items": [
    { "ticketId": "T-001", "content": "My order has not arrived." },
    { "ticketId": "T-002", "content": "How do I reset my password?" }
  ]
}
```

**Response — `202 Accepted`**

```json
{
  "jobId": "a3f9e2b1-...",
  "status": "PENDING",
  "message": "Job accepted. Track progress via WebSocket.",
  "itemCount": 2
}
```

The response is returned **immediately** — the job has been queued, not completed.

---

### `GET /api/jobs/{jobId}`

Returns the current state of a job.

**Response — `200 OK`**

```json
{
  "jobId": "a3f9e2b1-...",
  "status": "PROCESSING",
  "totalItems": 2,
  "processedItems": 1,
  "createdAt": "2026-07-13T19:00:00Z",
  "updatedAt": "2026-07-13T19:00:05Z"
}
```

**Job Status Values**

| Status | Description |
| :--- | :--- |
| `PENDING` | Job received and queued |
| `PROCESSING` | Worker is actively consuming items |
| `COMPLETED` | All items processed successfully |
| `FAILED` | Processing encountered an unrecoverable error |

---

## 🔌 WebSocket Events

The frontend connects using **STOMP over SockJS**.

**Endpoint:** `ws://localhost:8080/ws`

| Topic | Direction | Description |
| :--- | :--- | :--- |
| `/topic/jobs/{jobId}/progress` | Server → Client | Live processing progress |
| `/topic/jobs/{jobId}/status` | Server → Client | Status change notification |

**Example Progress Payload**

```json
{
  "jobId": "a3f9e2b1-...",
  "processedItems": 450,
  "totalItems": 1000,
  "percentComplete": 45.0,
  "currentItem": { "ticketId": "T-450", "sentiment": "NEGATIVE" }
}
```

---

## 🔷 Hexagonal Architecture

This project uses a **package-by-feature hexagonal** approach. Rather than a single global `domain/` package, each feature (`ticket_ingestion`, etc.) owns its own hexagon. This prevents features from coupling to each other through a shared domain layer.

```
  ticket_ingestion/  (one hexagon per feature)
  ┌───────────────────────────────────────────────────────────┐
  │                     infrastructure/                        │
  │  ┌─────────────┐  ┌──────────────────┐  ┌─────────────┐  │
  │  │ web/        │  │ messaging/        │  │ persistence/│  │
  │  │ REST Controller  RabbitMQ Pub+Con  │  │ JPA Adapter │  │
  │  │ (Driving    │  │ (Driving +        │  │ (Driven     │  │
  │  │  Adapter)   │  │  Driven Adapter)  │  │  Adapter)   │  │
  │  └──────┬──────┘  └────────┬──────────┘  └──────┬──────┘  │
  │         │                  │                    │          │
  │  ───────┼──────────────────┼────────────────────┼───────── │
  │         │        domain/ports/ (Interfaces)      │          │
  │  ───────┼──────────────────┼────────────────────┼───────── │
  │         │                  │                    │          │
  │  ┌──────▼──────────────────▼────────────────────▼──────┐  │
  │  │                    domain/                            │  │
  │  │  TicketBatch | SupportTicket | BatchStatus            │  │
  │  │       (Pure Java — zero framework imports)            │  │
  │  └───────────────────────────────────────────────────────┘  │
  └───────────────────────────────────────────────────────────┘
        ▲  Orchestrated by application/service/
           BatchIngestionService, BatchProcessingService
```

> **Rule:** `ticket_ingestion/domain/` must **never** import anything from `org.springframework`, `jakarta.persistence`, or `com.rabbitmq`. It is pure Java, fully testable without a Spring container.

---

## 🔄 Data Flow

Step-by-step trace of a single batch upload:

```
1. User uploads 1,000 tickets via the Angular dashboard.

2. Angular POSTs to POST /api/jobs.

3. JobIngestionService (domain):
   a. Creates a Job record with status = PENDING
   b. Calls JobRepository port → JPA adapter saves to PostgreSQL
   c. Calls MessagePublisherPort → RabbitMQ adapter publishes JobEvent
   d. Returns jobId to the REST controller

4. REST controller responds: 202 Accepted { jobId, status: "PENDING" }
   ── HTTP connection is now closed. The client is fully unblocked. ──

5. RabbitMQ holds the JobEvent in the processing queue.

6. JobProcessingService (async consumer) wakes up:
   a. Reads the JobEvent from the queue
   b. Iterates over each ticket item
   c. Runs heavy processing (AI sentiment analysis / mock sleep)
   d. Updates Job status → PROCESSING → COMPLETED in PostgreSQL
   e. After each item, emits a progress event via the WebSocket broadcaster

7. Angular dashboard receives live updates via WebSocket subscription
   and renders the real-time progress chart — no polling required.
```

---

## 🗺️ Development Roadmap

- [ ] **Phase 1 — Domain Model**: `TicketBatch`, `SupportTicket`, `BatchStatus` + port interfaces (`IngestBatchUseCase`, `BatchRepositoryPort`, `MessagePublisherPort`)
- [ ] **Phase 2 — Application Layer**: `BatchIngestionService` + `BatchProcessingService` use-case implementations
- [ ] **Phase 3 — Persistence Adapter**: JPA entities, Spring Data repositories, PostgreSQL JSONB column for raw payload
- [ ] **Phase 4 — Ingestion API**: `POST /api/jobs` REST controller wired to the ingestion use case
- [ ] **Phase 5 — RabbitMQ Integration**: Publisher adapter + `@RabbitListener` consumer + Dead Letter Queue (DLQ) config
- [ ] **Phase 6 — WebSocket**: STOMP config + live progress broadcaster
- [ ] **Phase 7 — Angular Dashboard**: Upload form, real-time progress chart, job list
- [ ] **Phase 8 — Docker Compose** *(deferred)*: Full local environment — PostgreSQL + RabbitMQ with a single `docker compose up`. Skipped for now; will be added once local Docker is available.
- [ ] **Phase 9 — Tests**: Domain unit tests (no container) + adapter integration tests (Testcontainers)

---

## 🛠️ Architecture Grid

| Layer | Technology | Core Responsibility |
| :--- | :--- | :--- |
| **Frontend UI** | Angular 17+ | Real-time WebSocket dashboard displaying progress and AI classification results |
| **Ingestion API** | Java 21 & Spring Boot 4 | Receives HTTP payload, saves `PENDING` state, fires event to queue, returns `202 Accepted` |
| **Message Broker** | RabbitMQ (AMQP) | Holds the backlog of tasks so the main API thread is never blocked |
| **Background Worker** | Java 21 async consumer | Listens to RabbitMQ, processes data, updates DB to `COMPLETED`, triggers WebSocket |
| **Database** | PostgreSQL (JSONB) | Stores job state and raw payload with schema flexibility via JSONB |
| **Architecture Pattern** | Hexagonal (Ports & Adapters) | Keeps domain business logic isolated from all framework and infrastructure concerns |