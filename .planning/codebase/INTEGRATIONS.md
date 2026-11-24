---
last_mapped_commit: dac447c9472a8c5ef74552942dd0c53a77535857
last_mapped_at: 2026-11-24
---
# External Integrations

**Analysis Date:** 2026-11-24

## APIs & External Services

**LLM (planned, not yet wired):**
- Russian LLM aggregator with an OpenAI-compatible API — the single external service the app will call.
  - SDK/Client: none yet. Plan is Spring `RestClient`/`WebClient` + Jackson (structured output parsed into DTOs) inside `src/main/java/ru/janeforjane/timeline/llm/`.
  - Auth: API key via env (`.env` file present; `application-local.yml` gitignored).
  - Current state: `src/main/java/ru/janeforjane/timeline/llm/LlmClient.java` is a **stub** — returns hardcoded `List.of("Шаблонная ветка A", "Шаблонная ветка B")` and logs. No network call is made.
  - Contract (`docs/project-description.md`): per-node response `{branches: [{consequence, next_dilemma?}], final_summary?}`. Fallback to template branches when the LLM is unreachable. Timeouts mandatory (rule in `src/main/java/ru/janeforjane/timeline/llm/AGENTS.md`).

## Data Storage

**Databases:**
- PostgreSQL — **planned**, not yet integrated. No JDBC driver, no JPA/`spring-boot-starter-data-jpa` in `pom.xml`.
  - Schema target: 3+ tables (`decision`, `node`, `edge`) per `docs/project-description.md`.
  - Connection: not configured (no `application.properties`/`yml` present; would come via `application-local.yml`/env).
  - Client: none yet.

**File Storage:**
- Local filesystem only. No cloud storage.

**Caching:**
- None in-memory/Redis. Planned: hash-keyed node cache (node + level) + per-session LLM call limit (`docs/project-description.md`). No cache library added.

## Authentication & Identity

**Auth Provider:**
- None. No Spring Security, no login, no session auth. Single-user educational MVP.

## Monitoring & Observability

**Error Tracking:**
- None (no Sentry/APM).

**Logs:**
- SLF4J + Logback (transitive via `spring-boot-starter-web` → `spring-boot-starter-logging`). Pattern: `private static final Logger log = LoggerFactory.getLogger(X.class)` in services and `llm/`. `System.out.println` is banned by Checkstyle (`RegexpSingleline` in `config/checkstyle/checkstyle.xml`).

## CI/CD & Deployment

**Hosting:**
- TimeWeb Cloud (VPS) — planned deployment target. Docker not required for MVP.

**CI Pipeline:**
- None detected (no GitHub Actions, GitLab CI, or other CI config files). Local gates only: `mvn test` + `mvn checkstyle:check` must be green before commit.

## Environment Configuration

**Required env vars:**
- LLM API key (for the future LLM call) — expected via `.env` / `application-local.yml`.
- No DB URL/credentials configured yet (PostgreSQL not wired).

**Secrets location:**
- `.env` (gitignored, present in repo root) and `application-local.yml` (gitignored, not yet created). Both are excluded from git via `.gitignore`.

## Webhooks & Callbacks

**Incoming:**
- None.

**Outgoing:**
- None (single POST to the future LLM endpoint; not implemented).

---

*Integration audit: 2026-11-24*
