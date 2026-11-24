---
last_mapped_commit: dac447c9472a8c5ef74552942dd0c53a77535857
last_mapped_at: 2026-11-24
---
# Codebase Structure

**Analysis Date:** 2026-11-24

## Directory Layout

```
timeline-project/
├── AGENTS.md                 # Agent instructions (stack, commands, conventions, prohibitions)
├── README.md                 # Project overview and navigation
├── pom.xml                   # Maven build: Spring Boot 3.3.5, Java 17, checkstyle + spotless
├── .gitignore                # Ignores target/, .idea/, .env, application-local.yml
├── .env                      # [present, gitignored] — environment secrets, never read
├── config/
│   └── checkstyle/
│       └── checkstyle.xml    # Checkstyle rules (SLF4J-only, naming, imports)
├── docs/
│   ├── about.md              # AI environment setup + screenshots
│   ├── memory.md             # Cross-session agent memory (handoff)
│   ├── plan.md               # Work plan with checkboxes
│   ├── project-description.md# Full product spec
│   └── screenshots/          # UI / e2e / linter screenshots
├── src/
│   ├── main/
│   │   ├── java/ru/janeforjane/timeline/
│   │   │   ├── TimelineApplication.java   # @SpringBootApplication entry point
│   │   │   ├── controller/
│   │   │   │   ├── DecisionController.java# POST /api/decision
│   │   │   │   └── dto/
│   │   │   │       ├── DecisionRequest.java # record {decision}
│   │   │   │       └── DecisionResponse.java# record {branches}
│   │   │   ├── service/
│   │   │   │   └── DecisionService.java    # business logic
│   │   │   └── llm/
│   │   │       ├── AGENTS.md               # module-local rules (timeout, fallback, structured output)
│   │   │       └── LlmClient.java          # LLM stub / future integration point
│   │   └── resources/
│   │       └── static/
│   │           └── index.html              # static single-page UI (TVA aesthetic)
│   └── test/
│       └── java/ru/janeforjane/timeline/
│           ├── controller/
│           │   └── DecisionPageE2eTest.java# Playwright e2e test
│           └── service/
│               └── DecisionServiceTest.java# JUnit 5 unit test
├── .opencode/                # Agent tooling (skills, hooks, plugins, opencode.json)
├── .planning/                # GSD planning state (codebase maps, phases)
└── target/                   # Maven build output (gitignored)
```

## Directory Purposes

**`src/main/java/ru/janeforjane/timeline/`:**
- Purpose: Application source root; package is the base for all production code.
- Contains: The four-layer package structure (`controller`, `service`, `llm`, planned `repository`).
- Key files: `TimelineApplication.java`, sub-packages below.

**`controller/`:**
- Purpose: HTTP ingress — request binding, validation, response mapping. No business logic, no LLM calls.
- Contains: `@RestController` classes and a `dto/` sub-package of `record` DTOs.
- Key files: `DecisionController.java`, `dto/DecisionRequest.java`, `dto/DecisionResponse.java`.

**`service/`:**
- Purpose: Business logic and orchestration; the only layer that calls into `llm/`.
- Contains: `@Service` classes with SLF4J logging.
- Key files: `DecisionService.java`.

**`llm/`:**
- Purpose: Isolated LLM integration boundary — all external LLM calls and fallbacks live here only.
- Contains: The `LlmClient` `@Component` plus a module-local `AGENTS.md` that enforces timeouts, fallback, and structured output.
- Key files: `LlmClient.java`, `AGENTS.md`.

**`src/main/resources/static/`:**
- Purpose: Static web assets served directly by Spring Boot.
- Contains: The single-page UI `index.html` (inline CSS + JS, `fetch` to `/api/decision`).
- Key files: `index.html`.

**`src/test/java/ru/janeforjane/timeline/`:**
- Purpose: Test source root mirroring the production package structure.
- Contains: `service/` unit tests and `controller/` e2e tests.
- Key files: `DecisionServiceTest.java`, `DecisionPageE2eTest.java`.

**`config/checkstyle/`:**
- Purpose: Build-time lint configuration consumed by the `maven-checkstyle-plugin`.
- Key files: `checkstyle.xml`.

**`docs/`:**
- Purpose: Human + agent documentation (spec, memory handoff, plan, setup).
- Key files: `project-description.md`, `memory.md`, `plan.md`, `about.md`.

## Key File Locations

**Entry Points:**
- `src/main/java/ru/janeforjane/timeline/TimelineApplication.java`: Spring Boot bootstrap (`@SpringBootApplication`, `main`).
- `src/main/java/ru/janeforjane/timeline/controller/DecisionController.java`: REST endpoint `POST /api/decision`.
- `src/main/resources/static/index.html`: browser UI, served at GET `/`.

**Configuration:**
- `pom.xml`: Maven build — parent `spring-boot-starter-parent` 3.3.5, `java.version=17`, checkstyle + spotless plugins.
- `config/checkstyle/checkstyle.xml`: lint rules (SLF4J-only, 120-char lines, naming, import hygiene).
- `.gitignore`: excludes `target/`, `.idea/`, `.env`, `application-local.yml`.
- No `src/main/resources/application.yml`/`.properties` — app uses Spring Boot defaults (port 8080).

**Core Logic:**
- `src/main/java/ru/janeforjane/timeline/service/DecisionService.java`: `generateBranches(String)` → `List<String>`.
- `src/main/java/ru/janeforjane/timeline/llm/LlmClient.java`: `generate(String)` → `List<String>` (stub fallback today).
- `src/main/java/ru/janeforjane/timeline/controller/dto/*.java`: request/response `record` DTOs.

**Testing:**
- `src/test/java/ru/janeforjane/timeline/service/DecisionServiceTest.java`: plain JUnit 5 unit test.
- `src/test/java/ru/janeforjane/timeline/controller/DecisionPageE2eTest.java`: Playwright + `@SpringBootTest(RANDOM_PORT)` e2e.

## Naming Conventions

**Files:**
- Java classes: `PascalCase.java` (e.g. `DecisionController.java`, `LlmClient.java`).
- Test classes: `*Test.java` mirroring the class under test (e.g. `DecisionServiceTest.java`).

**Directories:**
- Package directories are lowercase single nouns per layer: `controller`, `service`, `llm` (and planned `repository`).
- DTOs live in a `dto` sub-package under `controller/`.

**Classes/Records:**
- `record` names are `PascalCase` (e.g. `DecisionRequest`, `DecisionResponse`).

**Methods:**
- Production methods: `camelCase` (e.g. `generateBranches`).
- Test methods: `should_<result>_when_<condition>` with underscores (e.g. `should_return_non_empty_branches_when_decision_provided`) — enabled by the custom `MethodName` format in `checkstyle.xml`.

## Where to Add New Code

**New Feature (REST endpoint):**
- Primary code: add a `@RestController` in `src/main/java/ru/janeforjane/timeline/controller/`, with its DTOs in `controller/dto/`.
- Business logic: add a `@Service` in `src/main/java/ru/janeforjane/timeline/service/`.
- Tests: `src/test/java/ru/janeforjane/timeline/` mirroring the production package (`service/` or `controller/`).

**New LLM integration:**
- Implementation: replace/augment `src/main/java/ru/janeforjane/timeline/llm/LlmClient.java` (keep the `generate(String) → List<String>` contract). Do NOT add LLM calls anywhere outside `llm/`.
- Fallback/timeout rules: follow `src/main/java/ru/janeforjane/timeline/llm/AGENTS.md`.

**New persistence (planned):**
- Create `src/main/java/ru/janeforjane/timeline/repository/` for data access, add a datasource/ORM dependency to `pom.xml`, and configure it in `src/main/resources/application.yml` (not yet present).

**Utilities:**
- Shared helpers: put them in the package they serve (or a new `util/`/`common/` package under `ru.janeforjane.timeline` if cross-layer). There is no standalone `util/` package yet.

**Static UI assets:**
- HTML/JS/CSS: `src/main/resources/static/` (served at web root).

## Special Directories

**`target/`:**
- Purpose: Maven build output (compiled classes, jars, test reports).
- Generated: Yes (`mvn clean install` / `mvn test`).
- Committed: No (gitignored).

**`.opencode/`:**
- Purpose: Agent/IDE tooling — skills, hooks, plugins, `opencode.json` permissions.
- Generated: Partially (installed tooling).
- Committed: Yes.

**`.planning/`:**
- Purpose: GSD planning state (codebase maps under `.planning/codebase/`, phases, graphs).
- Generated: Yes (by GSD workflow).
- Committed: Yes.

**`docs/screenshots/`:**
- Purpose: Reference images for docs (UI, e2e-green, linter-hook).
- Generated: Yes (manual captures).
- Committed: Yes.

---

*Structure analysis: 2026-11-24*
