---
last_mapped_commit: dac447c9472a8c5ef74552942dd0c53a77535857
last_mapped_at: 2026-11-24
---
# Coding Conventions

**Analysis Date:** 2026-11-24

## Naming Patterns

**Files:**
- Java source files are `PascalCase` matching the class/record name (`DecisionService.java`, `DecisionRequest.java`).
- Package directories mirror the Java package path (`src/main/java/ru/janeforjane/timeline/controller/dto/`).

**Classes & Types:**
- `PascalCase` (enforced by Checkstyle `TypeName` regex `^[A-Z][a-zA-Z0-9]*$`).
- DTOs use Java `record` syntax: `src/main/java/ru/janeforjane/timeline/controller/dto/DecisionRequest.java`, `DecisionResponse.java`.

**Methods:**
- `camelCase` (Checkstyle `MethodName` regex `^[a-z][a-zA-Z0-9_]*$`).
- **Exception:** underscores are allowed specifically for the test naming convention `should_<result>_when_<condition>` — see `config/checkstyle/checkstyle.xml` and `docs/memory.md`.

**Variables & Parameters:**
- `camelCase`, lowercase first letter (Checkstyle `LocalVariableName` / `ParameterName` regex `^[a-z][a-zA-Z0-9]*$`).
- Class fields: `private final` for injected dependencies (e.g. `private final DecisionService decisionService;` in `DecisionController.java`).

**Static logger field:**
- `private static final Logger log = LoggerFactory.getLogger(<Class>.class);` — the canonical field name is `log` (see `DecisionService.java`, `LlmClient.java`).

**Tests:**
- Use the pattern `should_<result>_when_<condition>`, e.g. `should_return_non_empty_branches_when_decision_provided` in `DecisionServiceTest.java`, `should_render_branches_when_decision_submitted` in `DecisionPageE2eTest.java`.

## Code Style

**Formatting (Spotless):**
- Tool: `spotless-maven-plugin` (configured in `pom.xml`).
- Formatter: `googleJavaFormat` version `1.22.0`, AOSP style (4-space indent, no line-wrapping at 100 — AOSP uses 100-char lines).
- `removeUnusedImports` is enabled — do not leave unused imports.

**Linting (Checkstyle):**
- Tool: `maven-checkstyle-plugin` with custom config `config/checkstyle/checkstyle.xml` (Checkstyle `10.17.0`), severity `error`, `failsOnError=true`, and `includeTestSourceDirectory=true` (test sources are linted too).
- Max line length: **120** (`LineLength` module under `Checker`).
- No tab characters (`FileTabCharacter eachLine=true`) — use spaces.
- No star imports (`AvoidStarImport`), no unused/redundant imports.
- `IllegalImport` forbids packages `sun` and `lombok` — do NOT use Lombok; use explicit constructors.
- Braces required everywhere (`NeedBraces`); opening brace on same line (`LeftCurly`); whitespace rules enforced (`WhitespaceAround`, `WhitespaceAfter`, `GenericWhitespace`, `ParenPad`).
- Modifier order enforced (`ModifierOrder`), no redundant modifiers.

**Run commands:**
```bash
mvn checkstyle:check        # lint
mvn spotless:check          # format check (bound to default lifecycle goal `check`)
```

## Import Organization

**Order (observed in `DecisionController.java`, `DecisionService.java`):**
1. `java.*` / `javax.*` (JDK) — e.g. `import java.util.List;`
2. Third-party (`org.springframework.*`, `org.slf4j.*`, `com.fasterxml.*`)
3. Project-local (`ru.janeforjane.timeline.*`)

Static imports come first and are fully qualified, e.g. `import static org.junit.jupiter.api.Assertions.assertFalse;` (no `Assertions.*` star import).

**No path aliases** — plain fully-qualified imports only.

## Error Handling

**Current state:**
- No custom exceptions, `try/catch`, or `@ControllerAdvice` yet — the codebase is an early skeleton (`DecisionController.java`, `DecisionService.java`, `LlmClient.java`).
- `LlmClient.generate()` is a stub that always returns a fallback list and never throws.

**Required by module rules (`src/main/java/ru/janeforjane/timeline/llm/AGENTS.md`):**
- Every external call MUST have a timeout.
- Always implement a fallback: if the LLM is unavailable, return template branches per rules.
- Parse LLM responses into DTOs via structured output — never manipulate the raw JSON string.

## Logging

**Framework:** SLF4J (`org.slf4j.Logger` / `LoggerFactory`).

**Rules:**
- `System.out.println` / `System.err.println` are forbidden — enforced by Checkstyle `RegexpSingleline` with message `Используй SLF4J вместо System.out/err.println`.
- Use the placeholder syntax `{}`, never string concatenation: `log.info("Generating branches for decision: {}", decision);` (`DecisionService.java:21`).
- Logger is declared once per class as a `private static final Logger log` field.

## Comments

**When to comment:**
- Code is largely self-documenting; inline comments are rare in Java sources.
- Russian is used for prose in `AGENTS.md` (project + module), commit messages, and memory files (`docs/memory.md`).

**Javadoc:**
- Not used. No `/** ... */` doc blocks in any Java file — do not introduce them unless there is a reason.

## Function Design

**Size:** Small, single-responsibility methods. `DecisionController.decide()` delegates immediately to the service; `DecisionService.generateBranches()` logs then delegates to `LlmClient`.

**Parameters:**
- Records use single concise constructor parameters (`DecisionRequest(String decision)`).
- Controller methods accept DTOs via `@RequestBody`, not primitive parameters.

**Return Values:**
- API responses use DTO records (`DecisionResponse(List<String> branches)`), not raw maps or entities.
- Return immutable `List.of(...)` for fixed collections (`LlmClient.java:15`).

## Module Design

**Dependency injection:**
- Constructor injection only (no `@Autowired` field injection, no Lombok). Dependencies declared `private final` and set in an explicit constructor — e.g. `DecisionController(DecisionService decisionService)`, `DecisionService(LlmClient llmClient)`.

**Package layout (enforced by `AGENTS.md`):**
- `controller/` — HTTP layer: accept request, validate, respond.
- `service/` — business logic.
- `llm/` — LLM calls and fallback ONLY here.
- `repository/` — DB access (not yet present; PostgreSQL planned).
- `controller/dto/` — request/response records.

**Architectural rule:** LLM calls go only through `service/` → `llm/`, never directly from `controller/`.

**Barrel files / wildcard exports:** Not applicable (Java, no barrel pattern).

---

*Convention analysis: 2026-11-24*
