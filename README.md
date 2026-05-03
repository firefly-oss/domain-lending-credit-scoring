# domain-lending-credit-scoring

> Domain-layer orchestration microservice for credit scoring workflows in the Firefly lending platform.

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [API Endpoints](#api-endpoints)
- [Domain Logic](#domain-logic)
- [Dependencies](#dependencies)
- [Configuration](#configuration)
- [Running Locally](#running-locally)
- [Testing](#testing)

---

## Overview

`domain-lending-credit-scoring` is a reactive Spring WebFlux microservice that acts as the orchestration layer for credit scoring within the Firefly lending platform. It sits between experience-layer consumers (BFFs, API gateways) and the core platform service `core-lending-credit-scoring`, coordinating multi-step workflows through saga-based CQRS patterns.

The service exposes a clean domain-level REST API that abstracts away internal core-service concepts. A "scoring request" at the domain level corresponds to a `ScoringCase` in the core service; the identifier (`requestId`) is the same UUID across both layers. All endpoints are fully reactive, returning `Mono` publishers that integrate cleanly into non-blocking pipelines.

State-changing operations are driven through the Firefly framework's `SagaEngine` and `CommandBus`, ensuring that any partial failure during a multi-step workflow triggers automatic compensating transactions. Read operations flow through a `QueryBus` with a configurable cache TTL, reducing repeated load on the downstream service.

---

## Architecture

The service follows the **hexagonal (ports and adapters)** pattern layered inside a standard Maven multi-module build. The `-core` module contains all domain logic and is completely independent of HTTP or persistence concerns. The `-infra` module wires downstream SDK clients. The `-web` module provides the HTTP entry point. The `-sdk` module (generated from an OpenAPI specification) exposes the service's own contract to other consumers in the platform.

```
Experience Layer (BFF / API Gateway)
            │
            ▼
┌─────────────────────────────────────────┐
│  domain-lending-credit-scoring (-web)   │
│  CreditScoringController                │
│  Spring WebFlux · Port 8042             │
└───────────────┬─────────────────────────┘
                │ CreditScoringService
┌───────────────▼─────────────────────────┐
│  domain-lending-credit-scoring (-core)  │
│  CommandBus / QueryBus / SagaEngine     │
│  Sagas: InitiateScoringCaseSaga         │
│          ExecuteScoringProcessSaga      │
│  Handlers: InitiateScoringCaseHandler   │
│            RequestScoringHandler        │
│            GetScoringRequest/Result/    │
│            BureauReports/Models         │
└──────┬──────────────────────────────────┘
       │ SDK clients (WebClient-based)
┌──────▼───────────────────────────┐   ┌──────────────────────────────┐
│  core-lending-credit-scoring     │   │  credit-bureaus data store   │
│  (ScoringCaseApi,                │   │  (ScoringBureauCallApi –     │
│   ScoringRequestApi,             │   │   tracked inside core until  │
│   ScoringResultApi,              │   │   dedicated SDK ships)       │
│   ScoringModelApi,               │   │                              │
│   ScoringBureauCallApi)          │   │  Default: localhost:8089     │
│  Default: localhost:8085         │   └──────────────────────────────┘
└──────────────────────────────────┘
```

---

## Module Structure

| Module | Purpose |
|--------|---------|
| `domain-lending-credit-scoring-interfaces` | Domain-level DTOs intended for experience-layer consumption. Contains `CreditScoringCaseDTO`, which composes scoring case and result summary fields into a single flat representation. |
| `domain-lending-credit-scoring-core` | All domain logic: commands, queries, CQRS handlers, saga orchestrations, and the `CreditScoringService` interface with its implementation. Has no dependency on Spring Web or any persistence layer. |
| `domain-lending-credit-scoring-infra` | Infrastructure adapters. `CreditScoringClientFactory` builds WebClient-backed SDK clients for `core-lending-credit-scoring`; `CreditBureausClientFactory` builds the `ScoringBureauCallApi` client. Both factories read their base paths from `@ConfigurationProperties` beans. |
| `domain-lending-credit-scoring-web` | Spring Boot application entry point (`DomainLendingCreditScoringApplication`), `CreditScoringController`, and `application.yaml`. Activates `@EnableWebFlux`, `@ConfigurationPropertiesScan`, and OpenAPI documentation. |
| `domain-lending-credit-scoring-sdk` | Auto-generated OpenAPI client SDK exposing this service's contract to downstream consumers in the platform. Generated sources live under `target/generated-sources`. |

---

## API Endpoints

### Current endpoints (`/api/v1/scoring/...`)

| Method | Path | Description | Success Status |
|--------|------|-------------|----------------|
| `POST` | `/api/v1/scoring/requests` | Execute the full three-step credit scoring workflow (create case, fetch bureau data, compute result). Returns a `SagaResult`. | `200 OK` |
| `GET` | `/api/v1/scoring/requests/{requestId}` | Retrieve the current status of a scoring request (`ScoringCaseDTO`). | `200 OK` / `404 Not Found` |
| `GET` | `/api/v1/scoring/requests/{requestId}/result` | Retrieve the computed scoring result (`ScoringResultDTO`) for a request. | `200 OK` / `404 Not Found` |
| `GET` | `/api/v1/scoring/requests/{requestId}/bureau-reports` | Retrieve all credit bureau call records associated with a scoring request (`PaginationResponseScoringBureauCallDTO`). | `200 OK` / `404 Not Found` |
| `GET` | `/api/v1/scoring/models` | List all available scoring models (`PaginationResponseScoringModelDTO`). | `200 OK` |

### Legacy endpoints (`/api/v1/credit-scoring/...`)

These endpoints are retained for backward compatibility. New integrations should use the `/api/v1/scoring/` resource above.

| Method | Path | Description | Success Status |
|--------|------|-------------|----------------|
| `POST` | `/api/v1/credit-scoring/cases` | Open a new scoring case via the single-step `InitiateScoringCaseSaga`. | `200 OK` |
| `GET` | `/api/v1/credit-scoring/cases/{scoringCaseId}` | Retrieve a scoring case by its core-service identifier. | `200 OK` / `404 Not Found` |

**Request bodies:**

`POST /api/v1/scoring/requests` — `RequestScoringCommand`:
```json
{
  "applicationId": "<UUID>",   // required — loan application driving this request
  "partyId":       "<UUID>",   // required — customer to be scored
  "scoringModelId":"<UUID>"    // optional — override active model; null uses core default
}
```

`POST /api/v1/credit-scoring/cases` — `InitiateScoringCaseCommand`:
```json
{
  "loanApplicationId": "<UUID>",
  "customerId":        "<UUID>",
  "caseType":          "<string>"
}
```

Interactive API documentation (Swagger UI) is available at `http://localhost:8042/swagger-ui.html` on `dev` and `pre` profiles. It is disabled in `prod`.

---

## Domain Logic

### ExecuteScoringProcessSaga

The primary saga, invoked by `RequestScoringHandler` when a `RequestScoringCommand` arrives. It orchestrates the full credit scoring workflow in three sequential steps.

**Step 1 — `createScoringRequest`** (`compensate: markRequestFailed`)
- Calls `ScoringCaseApi.create1(ScoringCaseDTO, idempotencyKey)` on `core-lending-credit-scoring`.
- Populates the `ScoringCaseDTO` with `loanApplicationId` (from `cmd.getApplicationId()`) and `customerId` (from `cmd.getPartyId()`).
- Stores the returned `scoringCaseId` in the `ExecutionContext` under the key `"scoringCaseId"`.
- Emits: `scoring.request.created`

**Step 2 — `fetchBureauReport`** (depends on `createScoringRequest`)
- Reads `scoringCaseId` from the `ExecutionContext` (does not use the injected `cmd` argument, which may be null in dependent steps).
- Calls `ScoringBureauCallApi.create4(caseId, ScoringBureauCallDTO, idempotencyKey)`, recording an Equifax bureau interaction against the case with `isSuccess = true`.
- Returns the `scoringBureauCallId` to satisfy the saga engine's non-empty requirement.
- Emits: `scoring.bureau.fetched`

**Step 3 — `computeResult`** (depends on `fetchBureauReport`)
- Reads `scoringCaseId` from the `ExecutionContext`.
- Calls `ScoringRequestApi.create2(caseId, ScoringRequestDTO, idempotencyKey)` to create a `ScoringRequest`, which triggers score computation on the core service side.
- Returns the `scoringRequestId`.
- Emits: `scoring.result.computed`

**Compensation — `markRequestFailed`**
- Invoked automatically by the saga engine if any step fails.
- Calls `ScoringCaseApi.update1(caseId, patch, idempotencyKey)` with `caseStatus = CANCELLED`, ensuring the case is never left in a dangling `PENDING` state.
- Returns `Mono<Void>` (compensation methods may return empty).

---

### InitiateScoringCaseSaga (legacy)

A single-step saga invoked by `CreditScoringServiceImpl.initiateScoring()` for the legacy `POST /api/v1/credit-scoring/cases` endpoint.

**Step 1 — `initiateScoringCase`** (`compensate: removeScoringCase`)
- Dispatches `InitiateScoringCaseCommand` through the `CommandBus`, which routes it to `InitiateScoringCaseHandler`.
- `InitiateScoringCaseHandler` calls `ScoringCaseApi.create1(ScoringCaseDTO, idempotencyKey)`, mapping `loanApplicationId`, `customerId`, and `caseType` from the command.
- Stores the returned `scoringCaseId` in the `ExecutionContext`.
- Emits: `scoringCase.initiated`

**Compensation — `removeScoringCase`**
- Best-effort removal of the created case if a downstream step fails.
- Returns `Mono<Void>` via `Mono.empty()`. Errors are intentionally swallowed to avoid masking the root cause.

---

### CQRS Query Handlers

| Handler | Query | Downstream Call |
|---------|-------|-----------------|
| `GetScoringRequestHandler` | `GetScoringRequestQuery(requestId)` | `ScoringCaseApi.getById1(requestId)` |
| `GetScoringCaseHandler` | `GetScoringCaseQuery(scoringCaseId)` | `ScoringCaseApi.getById1(scoringCaseId)` |
| `GetScoringResultHandler` | `GetScoringResultQuery(requestId)` | Lists `ScoringRequest`s for the case (page 0, size 1), then lists `ScoringResult`s for the first request (page 0, size 1), and returns the first result. |
| `GetBureauReportsHandler` | `GetBureauReportsQuery(requestId)` | `ScoringBureauCallApi.findAll4(requestId, 0, 100, …)` |
| `GetScoringModelsHandler` | `GetScoringModelsQuery` | `ScoringModelApi.findAll(0, 100, …)` |

All GET operations pass no idempotency key (GET requests are inherently idempotent). Every mutating SDK call passes a freshly generated `UUID.randomUUID().toString()` as the idempotency key, making retries from saga compensation or Resilience4j safe.

---

## Dependencies

### Upstream (services this microservice consumes)

| Service | SDK | Purpose |
|---------|-----|---------|
| `core-lending-credit-scoring` | `com.firefly.core.lending.scoring.sdk` — `ScoringCaseApi`, `ScoringRequestApi`, `ScoringResultApi`, `ScoringModelApi`, `ScoringBureauCallApi` | Primary downstream. Stores scoring cases, requests, results, bureau calls, and scoring models. Base path configured via `api-configuration.core-platform.credit-scoring.base-path`. |
| Credit Bureaus data store | `ScoringBureauCallApi` (currently routed through `core-lending-credit-scoring`; will migrate to `core-data-credit-bureaus-sdk` when available) | Stores external credit-bureau interaction records. Base path configured via `api-configuration.core-data.credit-bureaus.base-path`. |

### Downstream (services that consume this microservice)

This service exposes its own client SDK as the `domain-lending-credit-scoring-sdk` module. Experience-layer services (BFFs, API gateways) import this SDK to communicate with the domain layer. The OpenAPI contract is published at `/v3/api-docs` (non-production profiles only).

---

## Configuration

All properties are defined in `domain-lending-credit-scoring-web/src/main/resources/application.yaml`.

### Application identity

| Property | Value |
|----------|-------|
| `spring.application.name` | `domain-lending-credit-scoring` |
| `spring.application.version` | `1.0.0` |
| `spring.application.description` | `Lending Domain Credit Scoring Orchestration Service` |
| `spring.application.team.name` | `Firefly Software Foundation` |
| `spring.application.team.email` | `dev@getfirefly.io` |

### Server

| Property | Default | Environment variable |
|----------|---------|----------------------|
| `server.address` | `localhost` | `SERVER_ADDRESS` |
| `server.port` | `8042` | `SERVER_PORT` |
| `server.shutdown` | `graceful` | — |

### Virtual threads

| Property | Value |
|----------|-------|
| `spring.threads.virtual.enabled` | `true` |

### Firefly CQRS framework

| Property | Value | Description |
|----------|-------|-------------|
| `firefly.cqrs.enabled` | `true` | Activates the CQRS command/query buses. |
| `firefly.cqrs.command.timeout` | `30s` | Maximum time allowed per command execution. |
| `firefly.cqrs.command.metrics-enabled` | `true` | Exposes command metrics to Micrometer. |
| `firefly.cqrs.command.tracing-enabled` | `true` | Attaches trace context to command executions. |
| `firefly.cqrs.query.timeout` | `15s` | Maximum time allowed per query execution. |
| `firefly.cqrs.query.caching-enabled` | `true` | Enables result caching for query handlers. |
| `firefly.cqrs.query.cache-ttl` | `15m` | Cache time-to-live for query results. |
| `firefly.saga.performance.enabled` | `true` | Enables saga performance tracking. |
| `firefly.stepevents.enabled` | `true` | Publishes `@StepEvent` domain events after each saga step. |

### Event-Driven Architecture (EDA)

| Property | Value | Description |
|----------|-------|-------------|
| `firefly.eda.enabled` | `true` | Activates the EDA publisher layer. |
| `firefly.eda.default-publisher-type` | `KAFKA` | Step events are published to Kafka. |
| `firefly.eda.default-connection-id` | `default` | References the `default` Kafka connection. |
| `firefly.eda.publishers.kafka.default.enabled` | `true` | Enables the default Kafka publisher. |
| `firefly.eda.publishers.kafka.default.default-topic` | `domain-layer` | Kafka topic for step events. |
| `firefly.eda.publishers.kafka.default.bootstrap-servers` | `localhost:9092` | Kafka bootstrap server address. |

### Downstream service URLs

| Property | Default | Environment variable |
|----------|---------|----------------------|
| `api-configuration.core-platform.credit-scoring.base-path` | `http://localhost:8085` | `CREDIT_SCORING_URL` |
| `api-configuration.core-data.credit-bureaus.base-path` | `http://localhost:8089` | `CREDIT_BUREAUS_URL` |

### OpenAPI / Swagger

| Property | Value |
|----------|-------|
| `springdoc.api-docs.path` | `/v3/api-docs` |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` |
| `springdoc.paths-to-match` | `/api/**` |

> Swagger UI and the OpenAPI spec endpoint are disabled in the `prod` profile.

### Actuator / Health

Exposed actuator endpoints: `health`, `info`, `prometheus`. Liveness and readiness probes are enabled. Health details are shown only to authorized users.

### Logging profiles

| Profile | Root level | `com.firefly` level | `org.springframework` |
|---------|-----------|---------------------|-----------------------|
| (default) | — | — | — |
| `dev` | `INFO` | `DEBUG` | — |
| `pre` | `INFO` | `INFO` | — |
| `prod` | `WARN` | `INFO` | `WARN` |

---

## Running Locally

Ensure the following services are running and accessible before starting:
- `core-lending-credit-scoring` on `http://localhost:8085` (or set `CREDIT_SCORING_URL`)
- Credit bureaus data endpoint on `http://localhost:8089` (or set `CREDIT_BUREAUS_URL`)
- Kafka broker on `localhost:9092`

```bash
# Build all modules, skipping tests
mvn clean install -DskipTests

# Start the application
cd /Users/casanchez/Desktop/firefly-oss/domain-lending-credit-scoring
mvn spring-boot:run -pl domain-lending-credit-scoring-web
```

Server port: **8042**

The Swagger UI is available at `http://localhost:8042/swagger-ui.html` once the service is running (disabled in `prod` profile).

To run with the `dev` profile for verbose logging:

```bash
mvn spring-boot:run -pl domain-lending-credit-scoring-web -Dspring-boot.run.profiles=dev
```

---

## Testing

```bash
mvn clean verify
```

Unit tests use JUnit 5 with Mockito and Project Reactor's `StepVerifier` for reactive assertion. The test suite covers:

- `RequestScoringHandlerTest` — verifies that `RequestScoringHandler` dispatches `ExecuteScoringProcessSaga` with the correct `StepInputs` and propagates saga errors faithfully.
- `GetScoringRequestHandlerTest` — verifies that `GetScoringRequestHandler` delegates to `ScoringCaseApi.getById1` and correctly propagates both present and empty responses.

Tests are located under `domain-lending-credit-scoring-core/src/test/`.
