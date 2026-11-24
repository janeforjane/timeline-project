---
last_mapped_commit: dac447c9472a8c5ef74552942dd0c53a77535857
last_mapped_at: 2026-11-24
---
# Technology Stack

**Analysis Date:** 2026-11-24

## Languages

**Primary:**
- Java 17 — entire backend. Declared via `java.version=17` in `pom.xml`; compiled under JDK 21 via `--release 17`.

**Secondary:**
- HTML + vanilla JavaScript — minimal frontend in `src/main/resources/static/index.html` (inline `<style>` + `fetch` to `/api/decision`). No frontend build tooling.
- CSS (inline) — TVA/Loki aesthetic (dark `#0b0b0f`, gold `#d4af37`), defined in `<style>` in `src/main/resources/static/index.html`.

## Runtime

**Environment:**
- Spring Boot 3.3.5 (parent POM `spring-boot-starter-parent`)
- Java runtime: JDK 21.0.13 (installed in dev env), but code targets Java 17 (`<java.version>17</java.version>`). Do not use 21-only language features.

**Package Manager:**
- Maven 3.8.7 (system-installed)
- Lockfile: not applicable (Maven has no lockfile). Dependency tree is resolved from `pom.xml`.
- No Maven wrapper (`mvnw`/`mvnw.cmd` and `.mvn/wrapper/` are absent) — use system `mvn`.

## Frameworks

**Core:**
- Spring Boot 3.3.5 — application shell (`@SpringBootApplication` in `src/main/java/ru/janeforjane/timeline/TimelineApplication.java`)
- Spring MVC (`spring-boot-starter-web`) — REST controller `src/main/java/ru/janeforjane/timeline/controller/DecisionController.java` + embedded Tomcat

**Testing:**
- JUnit 5 — via `spring-boot-starter-test` (test scope)
- Spring Boot Test (`@SpringBootTest`, `@LocalServerPort`) — integration/e2e tests
- Playwright Java 1.65.1 (`com.microsoft.playwright:playwright`, test scope) — browser e2e tests

**Build/Dev:**
- `spring-boot-maven-plugin` — run/package (`mvn spring-boot:run`)
- `maven-checkstyle-plugin` 3.5.0 + Checkstyle 10.17.0 — static analysis, bound to `validate` phase
- `spotless-maven-plugin` 2.43.0 + googleJavaFormat 1.22.0 (AOSP style) — code formatting, bound to default lifecycle (check goal)

## Key Dependencies

**Critical:**
- `spring-boot-starter-web` — MVC, JSON (Jackson), embedded Tomcat, SLF4J+Logback logging. This is the only compile-scope dependency.
- `spring-boot-starter-test` (test) — JUnit 5, AssertJ, Mockito, Spring Test, Jackson `ObjectMapper` for response parsing.

**Infrastructure:**
- `com.microsoft.playwright:playwright` 1.65.1 (test) — headless Chromium e2e tests. Browsers are NOT installed by default; install via `mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"`.

**Planned but NOT yet added (per `docs/project-description.md` and `AGENTS.md`):**
- PostgreSQL JDBC driver + Spring Data JPA — persistence layer (planned, not in `pom.xml`)
- Thymeleaf / HTMX — server-rendered frontend (planned; currently vanilla JS static page)
- OpenAI-compatible LLM HTTP client (e.g. Spring `RestClient`/`WebClient` + `Jackson`) — not yet added; `LlmClient` is a stub

## Configuration

**Environment:**
- `.env` file present (gitignored) — holds environment/secrets for dev; contents are never read by tooling.
- `application-local.yml` — planned for local overrides; gitignored. Not currently present.
- No `application.properties` / `application.yml` / `application.yaml` anywhere under `src/main/resources` — the app runs on Spring Boot defaults (port 8080).

**Build:**
- `pom.xml` — single source of build truth (parent, deps, plugins, `java.version`).
- `config/checkstyle/checkstyle.xml` — custom Checkstyle ruleset (referenced by `maven-checkstyle-plugin` `configLocation`).

## Platform Requirements

**Development:**
- JDK 17+ (dev env has JDK 21.0.13; pom compiles with `--release 17`)
- Maven 3.8.7+
- Node.js 22.x (for `opencode` tooling only — not required to build/run the Java app)
- Chromium browser installed for Playwright e2e tests

**Production:**
- TimeWeb Cloud (VPS) — deployment target per `docs/project-description.md`; Docker not required for MVP. Run as a standalone Spring Boot fat jar (`mvn clean package` → `java -jar target/*.jar`).

---

*Stack analysis: 2026-11-24*
