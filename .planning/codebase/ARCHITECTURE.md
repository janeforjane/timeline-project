---
last_mapped_commit: dac447c9472a8c5ef74552942dd0c53a77535857
last_mapped_at: 2026-11-24
---
<!-- refreshed: 2026-11-24 -->
# Architecture

**Analysis Date:** 2026-11-24

## System Overview

```text
┌─────────────────────────────────────────────────────────────┐
│                    HTTP Layer (Web / REST)                   │
├─────────────────────────────────────────────────────────────┤
│  static/index.html (fetch)  →  DecisionController            │
│  `src/main/resources/static/`   `src/main/java/.../controller/`│
└────────────────────────┬────────────────────────────────────┘
                         │  POST /api/decision  {decision}
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│       DecisionService  (business logic, logging)             │
│       `src/main/java/.../service/DecisionService.java`       │
└────────────────────────┬────────────────────────────────────┘
                         │  generateBranches(decision)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                        LLM Layer                             │
│       LlmClient  (stub today → OpenAI-compatible API later)  │
│       `src/main/java/.../llm/LlmClient.java`                 │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
        [PostgreSQL — PLANNED, not yet present]
        `repository/` layer planned per AGENTS.md, no tables yet
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| `TimelineApplication` | Spring Boot bootstrap; component scan root | `src/main/java/ru/janeforjane/timeline/TimelineApplication.java` |
| `DecisionController` | HTTP ingress: bind JSON request, delegate, map response | `src/main/java/ru/janeforjane/timeline/controller/DecisionController.java` |
| `DecisionRequest` | Inbound DTO (record): `{decision}` | `src/main/java/ru/janeforjane/timeline/controller/dto/DecisionRequest.java` |
| `DecisionResponse` | Outbound DTO (record): `{branches: []}` | `src/main/java/ru/janeforjane/timeline/controller/dto/DecisionResponse.java` |
| `DecisionService` | Business logic: orchestrate branch generation, logging | `src/main/java/ru/janeforjane/timeline/service/DecisionService.java` |
| `LlmClient` | LLM integration boundary; currently a stub fallback | `src/main/java/ru/janeforjane/timeline/llm/LlmClient.java` |
| `index.html` | Static single-page UI (TVA aesthetic) | `src/main/resources/static/index.html` |

## Pattern Overview

**Overall:** Layered / N-tier — `controller` → `service` → `llm` (and planned `repository`).

**Key Characteristics:**
- Strict dependency direction: HTTP layer depends on service; service depends on `llm`; nothing depends upward.
- Constructor injection everywhere (no `@Autowired`, no field injection, no Lombok).
- DTOs are Java `record` types for immutability.
- The `llm/` module is an isolated integration boundary governed by its own `AGENTS.md`.
- Single static HTML page (`index.html`) — no Thymeleaf/HTMX templates yet; plain `fetch` JSON against `/api/decision`.

## Layers

**Controller Layer:**
- Purpose: Accept HTTP requests, parse/validate input, produce HTTP response. No business logic, no LLM calls.
- Location: `src/main/java/ru/janeforjane/timeline/controller/`
- Contains: `@RestController` classes and `dto/` sub-package with `record` DTOs.
- Depends on: `service/` only.
- Used by: HTTP clients (browser `fetch` in `index.html`).

**Service Layer:**
- Purpose: Business logic and orchestration; the only layer allowed to call into `llm/`.
- Location: `src/main/java/ru/janeforjane/timeline/service/`
- Contains: `@Service` classes, SLF4J logging.
- Depends on: `llm/` (and later `repository/`).
- Used by: `controller/`.

**LLM Layer:**
- Purpose: All external LLM calls and their fallback behavior. The ONLY place LLM calls are permitted.
- Location: `src/main/java/ru/janeforjane/timeline/llm/`
- Contains: `@Component` integration client + its own `AGENTS.md` rules.
- Depends on: nothing internal (external OpenAI-compatible API later).
- Used by: `service/` only.

**Repository Layer (PLANNED — absent):**
- Purpose: PostgreSQL persistence (tables `decision`, `node`, `edge` per `docs/project-description.md`).
- Location: `src/main/java/ru/janeforjane/timeline/repository/` (not created yet).
- Note: no datasource/ORM dependency present in `pom.xml` yet.

## Data Flow

### Primary Request Path

1. Browser submits the form; `index.html` runs `fetch('/api/decision', {method:'POST', body: JSON.stringify({decision})})` (`src/main/resources/static/index.html:44`).
2. `DecisionController.decide()` binds `DecisionRequest` via `@RequestBody` and calls `decisionService.generateBranches(...)` (`src/main/java/ru/janeforjane/timeline/controller/DecisionController.java:20-24`).
3. `DecisionService.generateBranches()` logs the decision then delegates to `llmClient.generate(decision)` (`src/main/java/ru/janeforjane/timeline/service/DecisionService.java:20-23`).
4. `LlmClient.generate()` returns a `List<String>` of branches (`src/main/java/ru/janeforjane/timeline/llm/LlmClient.java:13-16`).
5. Controller wraps the list in `new DecisionResponse(branches)` and Spring serializes it to JSON `{branches: [...]}`.
6. Browser renders the JSON into `#result` (`src/main/resources/static/index.html:49-50`).

### Static Page Delivery

1. GET `/` is served automatically by Spring Boot's welcome-page mechanism from `src/main/resources/static/index.html` — there is no explicit `@GetMapping("/")` in the controller.

**State Management:**
- Stateless per request. No sessions, no server-side state, no caches yet. (Planned: LLM cache by hash `node+level` and per-session call limit — see `docs/project-description.md`.)

## Key Abstractions

**DTO records:**
- Purpose: Immutable request/response shape at the HTTP boundary.
- Examples: `DecisionRequest(String decision)`, `DecisionResponse(List<String> branches)`.
- Pattern: Java 17 `record` types, single-argument accessor methods used directly (e.g. `request.decision()`).

**LlmClient component:**
- Purpose: Single integration point for the LLM; isolates external I/O behind a stable `generate(String) → List<String>` contract so the service layer never knows about HTTP/timeouts/parsing.
- Pattern: `@Component` bean injected by constructor into the service.

## Entry Points

**Application bootstrap:**
- Location: `src/main/java/ru/janeforjane/timeline/TimelineApplication.java`
- Triggers: `mvn spring-boot:run` or a packaged jar.
- Responsibilities: `@SpringBootApplication` — component scan from `ru.janeforjane.timeline`, auto-configuration, embedded Tomcat on default port 8080.

**REST endpoint:**
- Location: `DecisionController#decide` (`POST /api/decision`)
- Triggers: browser form submission via `fetch`.
- Responsibilities: bind `{decision}`, delegate to service, return `{branches}`.

**Static UI:**
- Location: `src/main/resources/static/index.html`
- Triggers: GET `/`.
- Responsibilities: input form + inline JS that POSTs to `/api/decision` and renders the result.

## Architectural Constraints

- **Dependency direction:** `controller → service → llm` only. LLM calls must NEVER be invoked directly from `controller/` (enforced by `AGENTS.md` and `llm/AGENTS.md`).
- **Threading:** Standard Spring Boot embedded Tomcat — one thread per HTTP request; no shared mutable state, no async/`CompletableFuture` usage today.
- **Global state:** None. No module-level singletons or caches with mutable state.
- **Circular imports:** None — the layers form a strict acyclic graph.
- **Configuration:** No `src/main/resources/application.yml`/`.properties` present — the app runs on Spring Boot defaults (port 8080, no datasource). Environment secrets live in `.env` (gitignored) and `application-local.yml` (gitignored); neither is read by the current code.
- **Code style gate:** Build validates Checkstyle (`config/checkstyle/checkstyle.xml`) and Spotless (googleJavaFormat AOSP) on `validate`/`check` phases; `System.out` is forbidden — use SLF4J.

## Anti-Patterns

### Calling the LLM directly from the controller

**What happens:** A `@RestController` method imports `llm.LlmClient` and calls `.generate()` inline.
**Why it's wrong:** Bypasses the service layer, mixes HTTP concerns with external I/O, and violates the `llm/AGENTS.md` isolation rule (LLM calls only via `service → llm`).
**Do this instead:** Inject `DecisionService` into the controller and call `decisionService.generateBranches(...)`, exactly as `DecisionController.java:16-23` already does.

### Adding `System.out.println` for debugging

**What happens:** Debug output written via `System.out`/`System.err`.
**Why it's wrong:** `config/checkstyle/checkstyle.xml` bans it via `RegexpSingleline` (`System\.(out|err)\.print`) and the build fails on `validate`.
**Do this instead:** Declare `private static final Logger log = LoggerFactory.getLogger(X.class);` and use `log.info(...)` (pattern already used in `DecisionService.java:12` and `LlmClient.java:11`).

### Stub without timeout or fallback

**What happens:** `LlmClient.generate()` returns hardcoded branches with no network call, timeout, or error handling.
**Why it's wrong:** When the real OpenAI-compatible call is added, a hanging or failing LLM would block the request thread with no degradation path — violating `llm/AGENTS.md` (every external call needs a timeout; always implement fallback).
**Do this instead:** Implement the real client inside `llm/` with an explicit timeout and a fallback that returns template branches, keeping the `generate()` signature stable so `DecisionService` and its tests don't change.

## Error Handling

**Strategy:** No explicit error handling yet — the current stub path cannot fail.

**Patterns:**
- On invalid/missing `decision`, Spring's `@RequestBody` binding would return a 400 with no custom handler; no `@ControllerAdvice` or custom exception types exist yet.
- The fallback contract (`llm/AGENTS.md`) is the intended primary error-handling mechanism: degrade to template branches rather than propagate failures.

## Cross-Cutting Concerns

**Logging:** SLF4J + Logback (via Spring Boot). Loggers declared per class as `static final`. `DecisionService` logs decisions at INFO; `LlmClient` logs the stub generation.
**Validation:** None beyond HTML `required` on the input and Spring's JSON binding. No `jakarta.validation` constraints on the DTOs.
**Authentication:** None. No Spring Security dependency; single-user local app.
**Persistence:** None yet — PostgreSQL and a `repository/` layer are planned but absent from `pom.xml`.

---

*Architecture analysis: 2026-11-24*
