# Память между сессиями (ведёт worker)

> Читай в начале сессии, дописывай после каждой завершённой задачи.

## Сделано
- Скелет Maven-проекта Spring Boot 3.3.5 (Java 17), пакет `ru.janeforjane.timeline`.
- `TimelineApplication`, `DecisionController` (POST `/api/decision` → `{branches: []}`), DTO `DecisionRequest`/`DecisionResponse` (records).
- `DecisionService` подключил `LlmClient` (constructor injection) — теперь возвращает fallback-ветки `["Шаблонная ветка A", "Шаблонная ветка B"]` вместо пустого списка.
- `src/main/resources/static/index.html` — форма «введи решение → построить линию → показать заглушку» (fetch на `/api/decision`).
- e2e-тест `DecisionPageE2eTest` (Playwright Java 1.65.1, `@SpringBootTest` RANDOM_PORT): открывает `/`, заполняет `#decision`, кликает submit, парсит `#result` и проверяет непустой `branches`.
- `mvn test`, `mvn checkstyle:check` — зелёные (2 теста).

## Решения
- Спринг 3.3.5 как стабильная ветка 3.x; компиляция под Java 17 (`java.version=17`), в среде установлен JDK 21 — работает через `release`.
- Checkstyle: свой конфиг `config/checkstyle/checkstyle.xml`; версия checkstyle поднята до 10.17.0 (иначе `WhitespaceAround` ломается на records). Разрешены `_` в именах методов (`^[a-z][a-zA-Z0-9_]*$`) под конвенцию `should_..._when_...`.
- `allowEmptyTypes=true` в `WhitespaceAround`, чтобы пустые тела records `{}` не ругались.
- Spotless: googleJavaFormat AOSP + removeUnusedImports.
- GET `/` отдаёт static `index.html` автоматически через Spring Boot (welcome page), явный маппинг в контроллере не нужен.
- SLF4J-логгер только в сервисе; в контроллере LLM-логики нет.
- Playwright Java добавлен как test-зависимость (`com.microsoft.playwright:playwright`), браузеры ставятся через `mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"`.
- e2e-тест парсит ответ через Jackson `ObjectMapper` (Jackson уже в classpath от spring-boot-starter-web).

## Дальше
- Реальный LLM-слой в `llm/` (заменить stub `LlmClient` на OpenAI-совместимый вызов с таймаутом + structured output + fallback по правилам из `llm/AGENTS.md`).
- Кэш по хэшу (узел + уровень), лимит вызовов на сессию.

## Ловушки
- Checkstyle 9.3 (дефолт maven-checkstyle-plugin 3.5.0) не понимает records — `WhitespaceAround` падает на `{}`; фикс — checkstyle 10.x + `allowEmptyTypes`.
- `LineLength`, `FileTabCharacter`, `RegexpSingleline` (System.out) живут под `Checker`, а не `TreeWalker`.
- Тестовый метод с `_` не проходит стандартный паттерн `MethodName` — нужен свой format.
- В среде JDK 21, а не 17 — держать `java.version=17` в pom и не завязываться на 21-фичи.
- Playwright Java при первом `Playwright.create()` качает НЕ только chromium, а Firefox и WebKit тоже — первый прогон `mvn test` занял ~9 мин. После кэширования браузеров — секунды. В чистом CI нужно явно ставить только `install chromium` заранее.
- Playwright MCP (opencode) по умолчанию ищет канал `chrome` (`/opt/google/chrome/chrome`), которого нет — для скриншотов через MCP нужен `install chrome` или `--browser chromium`; e2e-тест на Java этот момент не затрагивает.