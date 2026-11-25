# Timeline Project — карта временной линии

## What This Is

Веб-приложение для визуализации дерева последствий при принятии решений (эстетика TVA из «Локи»). Пользователь вводит одно решение, LLM генерирует ветки-последствия (глубина 3, ширина 2: корень → 2 ветки → 4 последствия → 8 финалов), в конце пользователь получает сохраняемую «карту своей временной линии» с итогом. Учебный проект курса «AI-агенты» с прицелом на реальный деплой (российский PaaS для JVM-приложений — Amvera или аналогичный; финальный выбор на этапе деплоя, fat jar без Docker).

## Core Value

Пользователь вводит решение и получает наглядную, осмысленную карту временной линии. LLM-генерация веток (содержание) и атмосферная TVA-визуализация (форма) равнозначны: если падает любая из них, продукт теряет смысл.

## Requirements

### Validated

- ✓ Скелет Spring Boot 3.3.5 (Java 17) с `@SpringBootApplication` — existing
- ✓ REST-эндпоинт `POST /api/decision` принимает `{decision}`, возвращает `{branches}` — existing
- ✓ Слоистая архитектура `controller → service → llm` с constructor injection, DTO-записи (`record`) — existing
- ✓ Статическая страница `index.html` в TVA-эстетике с `fetch` на `/api/decision` — existing
- ✓ Заглушка LLM-клиента `LlmClient` с fallback-ветками — existing
- ✓ Тесты: unit (`DecisionServiceTest`) + Playwright e2e (`DecisionPageE2eTest`) — existing
- ✓ Билд-гейты: Checkstyle + Spotless, `mvn test` / `mvn checkstyle:check` зелёные — existing

### Active

- [ ] Полный MVP: экран ввода решения, экран графа (клик по узлу → «Продолжить эту ветку»), экран «Карта линии» с итогом, история сохранённых линий
- [ ] Реальный LLM-слой: OpenAI-совместимый вызов с таймаутом + structured output (`{branches: [{consequence, next_dilemma?}], final_summary?}`)
- [ ] Дерево последствий: глубина 3, ширина 2; на уровне 3 — `final_summary` вместо `next_dilemma`
- [ ] Персистентность: PostgreSQL, таблицы `decision`, `node`, `edge`
- [ ] Кэш по хэшу (узел + уровень) и лимит LLM-вызовов на сессию
- [ ] Fallback: при недоступности LLM — шаблонные ветки по правилам

### Out of Scope

- Реальное время / WebSocket — это инструмент, а не игра
- Тяжёлый фронтенд (React) — минимальный JS / Thymeleaf-HTML достаточно
- Docker для MVP — деплой как standalone fat jar
- Авторизация / многопользовательность — single-user локальный инструмент

## Context

- Существующий код: скелет ДЗ#3 (controller / service / llm-stub + статическая страница + 2 теста). Детали — `.planning/codebase/` и `docs/memory.md`.
- LLM: российский агрегатор (OpenAI-совместимый API). Секреты в `.env` (gitignored, агентом не читается). LLM-вызовы — только через `service → llm`, таймауты обязательны.
- Эстетика TVA: тёмный фон, золотая «священная линия», синие ветки, ретро-терминал («ВРЕМЕННОЕ УПРАВЛЕНИЕ. ВЕТВЛЕНИЕ ОБНАРУЖЕНО»).
- Конвенции: пакет `ru.janeforjane.timeline`; слои `controller` / `service` / `llm` / `repository`; тесты `should_..._when_...`; только SLF4J, без `System.out.println`.

## Constraints

- Технологический стек: Java 17, Spring Boot 3.3.5, Maven, PostgreSQL, Thymeleaf/HTMX или минимальный JS — зафиксировано в `pom.xml` и `AGENTS.md`
- Глубина дерева 3, ширина 2 — не раздувать
- Один LLM-вызов на узел (не «всё дерево сразу»)
- Таймауты на все внешние вызовы обязательны
- LLM-вызовы только через `service → llm`, не из controller
- Перед коммитом `mvn test` и `mvn checkstyle:check` зелёные
- Среда: JDK 21 в окружении, компиляция под Java 17 (`--release 17`)
- Деплой: российский PaaS для JVM (Amvera или аналогичный), fat jar без Docker — финальный выбор площадки на этапе деплоя

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Полный MVP как v1 | Выбор пользователя: ввод → дерево → карта + история | — Pending |
| PostgreSQL сразу в v1 | История и карта хранятся в БД (`decision`, `node`, `edge`) | — Pending |
| Core Value: генерация и визуализация равнозначны | Нельзя жертвовать одним ради другого | — Pending |
| `model_profile=inherit` (DeepSeek) | Сессия на opencode/DeepSeek; тиры Anthropic не применимы | ✓ Good |
| Слоистая архитектура `controller → service → llm` (+ planned `repository`) | Уже в коде и `AGENTS.md` | ✓ Good |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-11-25 after initialization*
