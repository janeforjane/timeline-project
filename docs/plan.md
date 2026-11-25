# План работы (ведёт worker)

> Читай в начале сессии, отмечай чекбоксы по ходу.

## Сделано: скелет проекта (ДЗ#3)
- [x] pom.xml (Spring Boot 3, Java 17, checkstyle + spotless + JUnit 5)
- [x] DecisionController (GET /, POST /api/decision)
- [x] DecisionService (заглушка)
- [x] один JUnit 5 тест
- [x] static/index.html (форма ввода решения)
- [x] `mvn test` и `mvn checkstyle:check` зелёные
- [x] e2e-тест (Playwright)

## Сделано: GSD-пайплайн (ДЗ#6)
- [x] /gsd-map-codebase — карта кода (.planning/codebase/, 7 файлов)
- [x] /gsd-settings — конфиг (.planning/config.json: inherit, TDD on, commit_docs on)
- [x] /gsd-new-project — PROJECT.md, REQUIREMENTS.md (16 v1), ROADMAP.md (4 фазы), STATE.md
- [x] /gsd-pause-work — handoff (.planning/.continue-here.md, HANDOFF.json)

## Дальше: Фаза 1 — LLM-слой и ввод решения
- [ ] /gsd-discuss-phase 1 (или /gsd-plan-phase 1)
- [ ] реальный LLM-слой в llm/ (OpenAI-совместимый, таймаут, structured output, fallback)
- [ ] кэш по хэшу (узел + уровень), лимит вызовов на сессию
- [ ] валидация пустого ввода
