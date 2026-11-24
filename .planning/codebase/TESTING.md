---
last_mapped_commit: dac447c9472a8c5ef74552942dd0c53a77535857
last_mapped_at: 2026-11-24
---
# Testing Patterns

**Analysis Date:** 2026-11-24

## Test Framework

**Runner:**
- JUnit 5 (via `spring-boot-starter-test`, Spring Boot 3.3.5).
- E2E driver: Playwright for Java `com.microsoft.playwright:playwright` version `1.65.1` (test scope, declared in `pom.xml`).

**Assertion Library:**
- JUnit 5 `org.junit.jupiter.api.Assertions` (`assertTrue`, `assertFalse`, etc.) — static-imported per assertion method.

**Run Commands:**
```bash
mvn test                        # run all tests
mvn test -Dtest=DecisionServiceTest          # single test class
mvn test -Dtest=DecisionPageE2eTest          # single E2E test
mvn checkstyle:check             # lint (run before committing)
```

## Test File Organization

**Location:**
- Co-located in the mirrored package tree under `src/test/java/ru/janeforjane/timeline/` — same sub-package as the class under test.

**Naming:**
- Test class = `<ClassUnderTest>Test`, e.g. `DecisionServiceTest.java`, `DecisionPageE2eTest.java`.
- Test method = `should_<result>_when_<condition>`, e.g. `should_return_non_empty_branches_when_decision_provided`.

**Structure:**
```
src/test/java/ru/janeforjane/timeline/
├── service/
│   └── DecisionServiceTest.java      # unit test for DecisionService
└── controller/
    └── DecisionPageE2eTest.java      # Playwright E2E test for the page
```

## Test Structure

**Suite organization (unit test):**
```java
class DecisionServiceTest {

    @Test
    void should_return_non_empty_branches_when_decision_provided() {
        DecisionService decisionService = new DecisionService(new LlmClient());   // Arrange

        List<String> branches = decisionService.generateBranches("Переехать в другой город"); // Act

        assertFalse(branches.isEmpty());                                          // Assert
        assertTrue(branches.contains("Шаблонная ветка A"));
        assertTrue(branches.contains("Шаблонная ветка B"));
    }
}
```
(from `src/test/java/ru/janeforjane/timeline/service/DecisionServiceTest.java`)

**Patterns:**
- Arrange / Act / Assert, one behavior per test.
- Test classes and methods are package-private (no `public` modifier).
- Setup/teardown in E2E use `static` `@BeforeAll` / `@AfterAll` (see `DecisionPageE2eTest.java:28-38`).

## Mocking

**Framework:** Mockito is on the classpath via `spring-boot-starter-test`, but is NOT currently used — there are no mocks in the codebase.

**Current approach:** Tests wire real collaborators via constructor injection (`new DecisionService(new LlmClient())`). This works because `LlmClient` is a stub returning a fixed `List.of("Шаблонная ветка A", "Шаблонная ветка B")`.

**Guidance for new tests:**
- Prefer real collaborators while the implementation is a pure stub.
- Once `LlmClient` performs real network calls, mock it with Mockito in unit tests and keep a stub/double for the service layer.

## Fixtures and Factories

**Test Data:**
- Inline literals, no factory methods. Russian decision strings used directly:
  - `"Переехать в другой город"` in both `DecisionServiceTest` and `DecisionPageE2eTest`.

**Location:**
- Inline in the test methods. No dedicated fixtures directory.

## Coverage

**Requirements:** None enforced — no JaCoCo, no coverage threshold configured.

**View Coverage:** Not available. Add JaCoCo (`jacoco-maven-plugin`) to `pom.xml` if coverage reporting is needed.

## Test Types

**Unit Tests:**
- Scope: single service method behavior (`DecisionServiceTest` covers `generateBranches` happy path).
- No persistence, no Spring context — plain JUnit 5 instantiation.

**Integration Tests:**
- Not yet present. No `@WebMvcTest` / `MockMvc` tests exist. When added, use `@WebMvcTest(DecisionController.class)` + `@MockBean`/Mockito to test the HTTP layer without a full context.

**E2E Tests:**
- Framework: Playwright Java.
- Annotation: `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` with `@LocalServerPort int port`.
- Lifecycle: `Playwright.create()` + `browser = playwright.chromium().launch()` in `@BeforeAll`; close both in `@AfterAll`.
- Per-test isolation: `try (BrowserContext context = browser.newContext()) { Page page = context.newPage(); ... }`.
- Verification pattern: fill a selector, click submit, `page.waitForFunction(...)`, then parse the DOM result with Jackson `ObjectMapper` and assert on the JSON (`DecisionPageE2eTest.java:40-57`).

## Common Patterns

**DOM-driven assertion (E2E):**
```java
page.fill("#decision", "Переехать в другой город");
page.click("button[type=submit]");
page.waitForFunction("() => document.getElementById('result').textContent.length > 0");
String text = page.textContent("#result");
JsonNode response = OBJECT_MAPPER.readTree(text);
JsonNode branches = response.get("branches");
assertTrue(branches.isArray(), "Ожидали поле branches в ответе");
assertFalse(branches.isEmpty(), "Ожидали непустой список веток");
```

**Static import style:** import each assertion method individually (`import static org.junit.jupiter.api.Assertions.assertFalse;`), not `Assertions.*`.

**Async Testing:** Not used. No `Awaitility`, no `@Timeout`.

**Error Testing:** Not present. No tests assert on exceptions (`assertThrows`) yet — add them when `LlmClient` gets real external-call error paths.

## Playwright Setup Caveats

- Install only Chromium for CI to avoid a long first run: `mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"`.
- `Playwright.create()` on a cold cache downloads Firefox and WebKit too — cache browsers before relying on fast `mvn test`.
- See `docs/memory.md` (section «Ловушки») for full details.

---

*Testing analysis: 2026-11-24*
