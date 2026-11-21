# Память между сессиями (ведёт worker)

> Читай в начале сессии, дописывай после каждой завершённой задачи.

## Сделано
- Скелет Maven-проекта Spring Boot 3.3.5 (Java 17), пакет `ru.janeforjane.timeline`.
- `TimelineApplication`, `DecisionController` (POST `/api/decision` → `{branches: []}`), `DecisionService` (заглушка, возвращает пустой список), DTO `DecisionRequest`/`DecisionResponse` (records).
- `src/main/resources/static/index.html` — форма «введи решение → построить линию → показать заглушку» (fetch на `/api/decision`).
- Тест `DecisionServiceTest.should_return_empty_branches_when_decision_provided` (JUnit 5, без Spring-контекста).
- `mvn test`, `mvn checkstyle:check`, `mvn spotless:check` — зелёные.

## Решения
- Спринг 3.3.5 как стабильная ветка 3.x; компиляция под Java 17 (`java.version=17`), в среде установлен JDK 21 — работает через `release`.
- Checkstyle: свой конфиг `config/checkstyle/checkstyle.xml`; версия checkstyle поднята до 10.17.0 (иначе `WhitespaceAround` ломается на records). Разрешены `_` в именах методов (`^[a-z][a-zA-Z0-9_]*$`) под конвенцию `should_..._when_...`.
- `allowEmptyTypes=true` в `WhitespaceAround`, чтобы пустые тела records `{}` не ругались.
- Spotless: googleJavaFormat AOSP + removeUnusedImports.
- GET `/` отдаёт static `index.html` автоматически через Spring Boot (welcome page), явный маппинг в контроллере не нужен.
- SLF4J-логгер только в сервисе; в контроллере LLM-логики нет.

## Дальше
- e2e-тест (Playwright) — следующий пункт plan.md.
- Подключить LLM-слой (`llm/`) и fallback по правилам из `llm/AGENTS.md`.

## Ловушки
- Checkstyle 9.3 (дефолт maven-checkstyle-plugin 3.5.0) не понимает records — `WhitespaceAround` падает на `{}`; фикс — checkstyle 10.x + `allowEmptyTypes`.
- `LineLength`, `FileTabCharacter`, `RegexpSingleline` (System.out) живут под `Checker`, а не `TreeWalker`.
- Тестовый метод с `_` не проходит стандартный паттерн `MethodName` — нужен свой format.
- В среде JDK 21, а не 17 — держать `java.version=17` в pom и не завязываться на 21-фичи.

docs/work/hw03/screenshots