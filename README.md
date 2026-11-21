# timeline — карта временной линии

![main-page.png](docs/screenshots/main-page.png)

Учебный проект курса «AI-агенты». Веб-приложение для визуализации дерева последствий
при принятии решений (эстетика TVA из «Локи»): пользователь вводит решение, LLM генерирует
ветки-последствия (до 3 уровней), в конце — «карта временной линии».

Стек: Java 17 · Spring Boot 3.3.5 · Maven · PostgreSQL (позже) · Thymeleaf/HTMX ·
JUnit 5 · Checkstyle · Spotless. Агент разработки: OpenCode 1.19.0 (DeepSeek).

Полное описание проекта — `docs/project-description.md`.
Настройка AI-среды и скриншоты — `docs/about.md`.

## Как ориентироваться в репозитории

- `AGENTS.md` — инструкции для агента (стек, команды, конвенции, запреты).
- `src/main/java/ru/janeforjane/timeline/llm/AGENTS.md` — правило, действующее только на модуль `llm/`.
- `.opencode/` — скилл `write-tests`, команда `/wrong-branch`, плагин-hooks `project-guard.ts`,
  конфиг прав `opencode.json`.
- `docs/plan.md` + `docs/memory.md` — память агента между сессиями (handoff).
- `docs/about.md` — подробно о настройке среды + скриншоты.

## Запуск

```bash
mvn spring-boot:run    # http://localhost:8080