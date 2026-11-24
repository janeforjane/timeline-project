---
last_mapped_commit: dac447c9472a8c5ef74552942dd0c53a77535857
last_mapped_at: 2026-11-24
---
# Codebase Concerns

**Analysis Date:** 2026-11-24

> Проект находится на стадии скелета (ДЗ#3, фаза 2). Реальный LLM-слой, БД,
> кэш и лимиты — ещё не реализованы. Ниже — долг, известные проблемы и риски,
> которые надо учесть до того, как функционал зарастёт.

## Tech Debt

**LLM-клиент — заглушка вместо реализации:**
- Issue: `LlmClient.generate()` возвращает жёстко зашитые `List.of("Шаблонная ветка A", "Шаблонная ветка B")`, не делая ни одного внешнего вызова.
- Files: `src/main/java/ru/janeforjane/timeline/llm/LlmClient.java`
- Impact: вся AI-фича — плейсхолдер. Нет таймаута, нет structured output, нет fallback-правил (требования `src/main/java/ru/janeforjane/timeline/llm/AGENTS.md` формально не выполнены — «таймаут» и «structured output» просто не к чему применять, пока вызов не написан).
- Fix approach: реализовать OpenAI-совместимый вызов через `RestClient`/`WebClient` с `@Value` для base-url/API-key, таймаутом и парсингом в DTO; текущие «Шаблонная ветка A/B» оставить как fallback-ветку.

**Нет слоя доступа к БД:**
- Issue: PostgreSQL заявлен в стеке (`docs/project-description.md` — 3+ таблицы `decision`, `node`, `edge`), но в `pom.xml` нет ни `spring-boot-starter-data-jpa`, ни `spring-boot-starter-jdbc`; нет `repository/` пакета, нет схемы, нет миграций.
- Files: `pom.xml`, `docs/project-description.md`
- Impact: историю сохранённых линий и «карту линии» не на чем строить.
- Fix approach: добавить JPA/JDBC-стартер, схему и (Flyway/Liquibase) миграции, прежде чем писать экран истории.

**Нет кэша и лимита вызовов:**
- Issue: контроль стоимости (кэш по хэшу «узел+уровень», лимит вызовов на сессию) заявлен в `docs/project-description.md` (раздел «AI-фича») и `docs/memory.md` («Дальше»), но нигде не реализован.
- Files: `src/main/java/ru/janeforjane/timeline/service/DecisionService.java`, `docs/memory.md`
- Impact: как только появится реальный LLM, каждый клик по узлу = платный вызов без дедупликации и без потолка на сессию.
- Fix approach: ввести кэш-ключ от `(decision, level)` и счётчик/лимит на сессию до подключения реального клиента.

**Нет Thymeleaf/HTMX (заявлено в стеке):**
- Issue: README и `docs/project-description.md` упоминают Thymeleaf/HTMX, но фронт — единый статический `index.html` с ручным `fetch`.
- Files: `src/main/resources/static/index.html`, `README.md`
- Impact: рост интерфейса на голом JS без шаблонизации будет хрупким (см. «Fragile Areas»).

## Known Bugs

**Отсутствует валидация входящего `decision`:**
- Symptoms: POST `/api/decision` с пустым телом `{}` или `{"decision":null}` принимается; `DecisionRequest.decision()` становится `null`, уходит в `LlmClient.generate(null)` и логируется как `null`, возвращая fallback-ветки.
- Files: `src/main/java/ru/janeforjane/timeline/controller/dto/DecisionRequest.java`, `src/main/java/ru/janeforjane/timeline/controller/DecisionController.java`
- Trigger: `curl -X POST localhost:8080/api/decision -H 'Content-Type: application/json' -d '{}'`
- Workaround: вручную проверить поле на клиенте; серверных гарантий нет.
- Fix: добавить `@Valid @NotBlank @Size(max=...)` на поле, `spring-boot-starter-validation` в `pom.xml`, и маппинг ошибки (400).

**Фронт не обрабатывает ошибки запроса:**
- Symptoms: если `/api/decision` вернёт 4xx/5xx или не-JSON, `response.json()` бросает исключение, блок `#result` остаётся пустым/старым, в UI нет сообщения об ошибке.
- Files: `src/main/resources/static/index.html` (строки 41-51)
- Trigger: отправить форму при упавшем бэкенде или таймауте.
- Fix: обернуть `fetch`/`await response.json()` в `try/catch`, проверять `response.ok`, показывать сообщение в `#result`.

## Security Considerations

**Без валидации и длины — будущий prompt injection / cost abuse:**
- Risk: `decision` без ограничения длины и без `@NotBlank` уйдёт прямиком в промпт LLM; злоумышленник может инжектить инструкции или раздувать стоимость запросов.
- Files: `src/main/java/ru/janeforjane/timeline/controller/dto/DecisionRequest.java`, `src/main/java/ru/janeforjane/timeline/llm/LlmClient.java`
- Current mitigation: вызова LLM нет (заглушка), поэтому сейчас реального риска нет.
- Recommendations: ввести `@Size`/лимит символов, экранировать/обрамлять пользовательский ввод в промпте, добавить лимит вызовов на сессию до интеграции LLM.

**Нет аутентификации и rate limiting:**
- Risk: `/api/decision` открыт без ограничений; после подключения платного LLM это прямой канал для исчерпания бюджета.
- Files: `src/main/java/ru/janeforjane/timeline/controller/DecisionController.java`
- Current mitigation: отсутствует (MVP, локально).
- Recommendations: rate limit на сессию/IP и (при необходимости) базовая авторизация до деплоя.

**XSS в будущей отрисовке веток:**
- Risk: сейчас результат выводится через `textContent` (безопасно), но при переходе к отрисовке HTML-графа легко свалиться на `innerHTML` с контентом от LLM.
- Files: `src/main/resources/static/index.html` (строка 50)
- Current mitigation: `textContent` на текущий момент.
- Recommendations: при отрисовке графа использовать `textContent`/экранирование, не `innerHTML` с неэкранированным текстом от LLM.

## Performance Bottlenecks

**Первый прогон e2e-теста скачивает браузеры:**
- Problem: `Playwright.create()` при первом запуске качает не только chromium — `mvn test` в первый раз занимал ~9 минут (зафиксировано в `docs/memory.md`).
- Files: `src/test/java/ru/janeforjane/timeline/controller/DecisionPageE2eTest.java` (строка 30)
- Cause: playwright загружает chromium + Firefox + WebKit.
- Improvement path: в чистом CI заранее ставить только chromium (`... -Dexec.args="install chromium"`), как описано в `docs/memory.md`; в e2e-тесте использовать `playwright.chromium()` (уже так).

**Нет кэша LLM (повторные вызовы = лишняя стоимость):**
- Problem: каждый запрос заново генерирует ветки без дедупликации по `(decision, level)`.
- Files: `src/main/java/ru/janeforjane/timeline/service/DecisionService.java`
- Cause: кэш заявлен, но не реализован.
- Improvement path: ввести кэш по хэшу «узел+уровень» до подключения реального LLM (см. Tech Debt).

## Fragile Areas

**Конфиг Checkstyle завязан на workaround для records:**
- Files: `config/checkstyle/checkstyle.xml` (строки 35-37), `pom.xml` (строки 55-60)
- Why fragile: версия checkstyle поднята до `10.17.0` через `<dependencies>` внутри `maven-checkstyle-plugin:3.5.0`, а `WhitespaceAround` включает `allowEmptyTypes=true`, чтобы пустые тела records `{}` не падали. Любое обновление плагина/чекстайла может снова сломать проверку records.
- Safe modification: при апгрейде checkstyle/плагина прогонять `mvn checkstyle:check` сразу после, не менять `allowEmptyTypes` без проверки.
- Test coverage: правит сам линтер (валидируется на `mvn checkstyle:check`), но неявно.

**Рассогласование JDK: в среде 21, в проекте 17:**
- Files: `pom.xml` (строка 21)
- Why fragile: в среде установлен JDK 21, проект компилируется под 17 через `java.version=17`/`release`. Случайное использование 21-фич (например, `switch` по паттернам, виртуальные потоки) сломает сборку на 17.
- Safe modification: держать `java.version=17`, не использовать API новее 17; периодически проверять `mvn clean compile`.

**e2e-тест жёстко завязан на селекторы DOM:**
- Files: `src/test/java/ru/janeforjane/timeline/controller/DecisionPageE2eTest.java` (строки 46-50)
- Why fragile: тест ищет `#decision`, `button[type=submit]`, `#result` — любые правки `index.html` (переименование id, смена кнопки) молча ломают тест.
- Safe modification: держать контракт id `#decision`/`#result` стабильным или вынести в константы; при смене UI обновлять тест в том же коммите.
- Test coverage: сам e2e-тест является покрытием счастливого пути; негативных сценариев нет.

**Единый рукописный статический HTML:**
- Files: `src/main/resources/static/index.html`
- Why fragile: вся вёрстка, стили и JS — в одном файле без шаблонизации; рост интерфейса (граф, карта линии, история) будет накапливать дублирование.
- Safe modification: при появлении второго экрана перейти на Thymeleaf/HTMX-фрагменты, как заявлено в стеке.

## Scaling Limits

**Ограничения дерева зашиты в дизайн, но не в код:**
- Current capacity: дизайн фиксирует глубину 3 / ширину 2 (до 8 финалов) — `docs/project-description.md`.
- Limit: в коде нет ни проверки уровня, ни ограничения ширины; сервис безразличен к глубине.
- Scaling path: завести явные константы/проверку глубины (MAX_DEPTH=3) в сервисе до реализации дерева, чтобы не допустить взрывной рост вызовов LLM.

**Нет управления сессиями:**
- Current capacity: состояние (введённое решение, построенное дерево) нигде не хранится между запросами.
- Limit: «карта временной линии» и «история» потребуют хранения графа на сессию/пользователя.
- Scaling path: определить модель хранения (сессия in-memory → PostgreSQL) до экрана «Карта линии».

## Dependencies at Risk

**Spring Boot 3.3.5 (устаревшая ветка 3.x):**
- Risk: 3.3.x — не последняя ветка 3.x; накопление багфиксов/секьюрити-патчей недоступно без апгрейда.
- Impact: возможны незакрытые CVE в starter-ах; для учебного MVP некритично.
- Migration plan: при первой удобной паузе поднять до актуальной 3.x/3.4.x и прогнать `mvn test` + `mvn checkstyle:check`.

**Playwright 1.65.1 (тяжёлая test-зависимость):**
- Risk: скачивает браузеры при первом запуске, тянет размер сборки тестов.
- Impact: медленный холодный CI/локальный прогон (см. Performance).
- Migration plan: закрепить установку только chromium в CI; при переходе на частые e2e рассмотреть профиль запуска.

**maven-checkstyle-plugin 3.5.0 + checkstyle 10.17.0 (версии разнесены):**
- Risk: checkstyle переопределён отдельной зависимостью поверх дефолта плагина — хрупкая связка (см. Fragile Areas).
- Impact: апгрейд плагина без синхронного апгрейда чекстайла может вернуть баг с records.
- Migration plan: держать пару версий вместе; при апгрейде проверять records-конфиг.

## Missing Critical Features

**Реальный LLM-вызов с таймаутом и structured output:**
- Problem: `LlmClient` — заглушка; нет вызова к OpenAI-совместимому API, нет таймаута, нет DTO для structured output (`{branches:[{consequence, next_dilemma?}], final_summary?}` из `docs/project-description.md`).
- Blocks: генерацию реальных веток, уровня 3 с `final_summary`, «карту временной линии».

**Схема БД и миграции:**
- Problem: нет таблиц `decision`/`node`/`edge`, нет ORM/репозиториев.
- Blocks: сохранение линий и историю.

**Кэш по хэшу и лимит на сессию:**
- Problem: заявлен, не реализован.
- Blocks: безопасную (по стоимости) интеграцию LLM.

## Test Coverage Gaps

**Валидация и ошибки контроллера:**
- What's not tested: пустой/null `decision`, не-JSON тело, код 400 при невалидном вводе, обработка ошибки фронта.
- Files: `src/main/java/ru/janeforjane/timeline/controller/DecisionController.java`, `src/main/java/ru/janeforjane/timeline/controller/dto/DecisionRequest.java`
- Risk: регрессии валидации пройдут незамеченными.
- Priority: Medium.

**`LlmClient` как отдельный компонент:**
- What's not tested: `LlmClient.generate()` не имеет собственного теста — покрывается косвенно через `DecisionServiceTest`, который проверяет саму заглушку (`"Шаблонная ветка A"`).
- Files: `src/main/java/ru/janeforjane/timeline/llm/LlmClient.java`, `src/test/java/ru/janeforjane/timeline/service/DecisionServiceTest.java`
- Risk: тест утверждает поведение плейсхолдера; после замены на реальный клиент тест придётся переписывать (завязка на константы).
- Priority: High — переписать на mock `LlmClient`, не на его конкретную реализацию.

**DTO-маппинг ответа:**
- What's not tested: сериализация/десериализация `DecisionRequest`/`DecisionResponse`.
- Files: `src/main/java/ru/janeforjane/timeline/controller/dto/DecisionRequest.java`, `src/main/java/ru/janeforjane/timeline/controller/dto/DecisionResponse.java`
- Risk: изменение формы JSON сломает контракт незаметно.
- Priority: Low.

**Покрытие в целом:**
- What's not tested: всего 2 теста (`DecisionServiceTest`, `DecisionPageE2eTest`); нет юнит-теста контроллера, нет тестов ошибок, нет теста валидации.
- Files: `src/test/java/ru/janeforjane/timeline/service/DecisionServiceTest.java`, `src/test/java/ru/janeforjane/timeline/controller/DecisionPageE2eTest.java`
- Risk: скелет тонкий; по мере роста функционала покрытие отстанет.
- Priority: Medium.

---

*Concerns audit: 2026-11-24*
