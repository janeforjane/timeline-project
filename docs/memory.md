# Память между сессиями (ведёт worker)

> Читай в начале сессии, дописывай после каждой завершённой задачи.

## Сделано
- Скелет Maven-проекта Spring Boot 3.3.5 (Java 17), пакет `ru.janeforjane.timeline`.
- `TimelineApplication`, `DecisionController` (POST `/api/decision` → `{branches: []}`), DTO `DecisionRequest`/`DecisionResponse` (records).
- `DecisionService` подключил `LlmClient` (constructor injection) — теперь возвращает fallback-ветки `["Шаблонная ветка A", "Шаблонная ветка B"]` вместо пустого списка.
- `src/main/resources/static/index.html` — форма «введи решение → построить линию → показать заглушку» (fetch на `/api/decision`).
- e2e-тест `DecisionPageE2eTest` (Playwright Java 1.65.1, `@SpringBootTest` RANDOM_PORT): открывает `/`, заполняет `#decision`, кликает submit, парсит `#result` и проверяет непустой `branches`.
- `mvn test`, `mvn checkstyle:check` — зелёные (2 теста).
- GSD-пайплайн установлен, проект инициализирован: `.planning/PROJECT.md`, `REQUIREMENTS.md` (16 v1-требований), `ROADMAP.md` (4 фазы, режим MVP), `STATE.md`.
- Конфиг GSD (`/gsd-settings`): `model_profile=inherit`, TDD on, commit_docs on, branching none, worktrees off.
- `/gsd-pause-work` — handoff `.planning/.continue-here.md` + `.planning/HANDOFF.json`.

## Решения
- Спринг 3.3.5 как стабильная ветка 3.x; компиляция под Java 17 (`java.version=17`), в среде установлен JDK 21 — работает через `release`.
- Checkstyle: свой конфиг `config/checkstyle/checkstyle.xml`; версия checkstyle поднята до 10.17.0 (иначе `WhitespaceAround` ломается на records). Разрешены `_` в именах методов (`^[a-z][a-zA-Z0-9_]*$`) под конвенцию `should_..._when_...`.
- `allowEmptyTypes=true` в `WhitespaceAround`, чтобы пустые тела records `{}` не ругались.
- Spotless: googleJavaFormat AOSP + removeUnusedImports.
- GET `/` отдаёт static `index.html` автоматически через Spring Boot (welcome page), явный маппинг в контроллере не нужен.
- SLF4J-логгер только в сервисе; в контроллере LLM-логики нет.
- Playwright Java добавлен как test-зависимость (`com.microsoft.playwright:playwright`), браузеры ставятся через `mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"`.
- e2e-тест парсит ответ через Jackson `ObjectMapper` (Jackson уже в classpath от spring-boot-starter-web).
- Объём v1 — полный MVP (ввод → дерево → карта + история), PostgreSQL сразу в v1.
- Core Value: LLM-генерация и TVA-визуализация равнозначны.
- `model_profile=inherit` — сессия opencode/DeepSeek, тиры Anthropic не применимы.
- Деплой — российский PaaS для JVM (Amvera или аналог), fat jar без Docker (вместо TimeWeb Cloud).
- Структура фаз — Vertical MVP (вертикальные срезы).

## Дальше
- Фаза 1 (GSD): реальный LLM-слой в `llm/` (OpenAI-совместимый вызов с таймаутом + structured output + fallback по правилам из `llm/AGENTS.md`) + валидация ввода.
- Фаза 2: дерево последствий 3×2 (TVA-эстетика).
- Фаза 3: PostgreSQL (таблицы `decision`/`node`/`edge`).
- Фаза 4: карта линии + история.

## Ловушки
- Checkstyle 9.3 (дефолт maven-checkstyle-plugin 3.5.0) не понимает records — `WhitespaceAround` падает на `{}`; фикс — checkstyle 10.x + `allowEmptyTypes`.
- `LineLength`, `FileTabCharacter`, `RegexpSingleline` (System.out) живут под `Checker`, а не `TreeWalker`.
- Тестовый метод с `_` не проходит стандартный паттерн `MethodName` — нужен свой format.
- В среде JDK 21, а не 17 — держать `java.version=17` в pom и не завязываться на 21-фичи.
- Playwright Java при первом `Playwright.create()` качает НЕ только chromium, а Firefox и WebKit тоже — первый прогон `mvn test` занял ~9 мин. После кэширования браузеров — секунды. В чистом CI нужно явно ставить только `install chromium` заранее.
- Playwright MCP (opencode) по умолчанию ищет канал `chrome` (`/opt/google/chrome/chrome`), которого нет — для скриншотов через MCP нужен `install chrome` или `--browser chromium`; e2e-тест на Java этот момент не затрагивает.
- `~/.gsd/defaults.json` (глобальные дефолты GSD) не сохраняются — opencode запрещает доступ к внешним директориям (`external_directory` deny). Фикс: разрешить `~/.gsd/**` в `.opencode/opencode.json`.