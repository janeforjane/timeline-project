# ДЗ#3 «Настройка AI-ориентированной среды»

## Стек и суть
Веб-приложение для визуализации дерева последствий решений (эстетика TVA из «Локи»).
Стек: Java 17 · Spring Boot 3.3.5 · Maven · PostgreSQL (позже) · Thymeleaf/HTMX ·
JUnit 5 · Checkstyle · Spotless. Агент: OpenCode 1.19.0 на DeepSeek.

## Старт

Настройка окружения проведена. Начинаем с формирования простого Maven-проекта и скелета приложения

```md
Создай минимальный Maven-проект Spring Boot 3 (Java 17) в корне репозитория.

Требования:
- pom.xml: spring-boot-starter-web, spring-boot-starter-test, maven-checkstyle-plugin,
  spotless-maven-plugin, JUnit 5.
- Пакет ru.janeforjane.timeline.
- DecisionController: GET / — отдаёт статический index.html; POST /api/decision — принимает JSON
  {decision}, возвращает заглушку {branches: []}.
- DecisionService — заглушка бизнес-логики (пока без LLM, просто возвращает фиксированный ответ).
- Один JUnit 5 тест на DecisionService (имя should_..._when_...).
- src/main/resources/static/index.html — простая форма «введи решение → кнопка → показать заглушку».

Правила:
- Только SLF4J, никаких System.out.println.
- LLM-вызовы — только через service-слой (пока их нет, но не клади ничего LLM-шного в controller).
- Перед финишем запусти mvn test и mvn checkstyle:check, чтобы были зелёными.
- По завершении обнови docs/memory.md (сделано / решение / дальше / ловушки) и отметь чекбоксы в docs/plan.md.
```

![todo.png](screenshots/todo.png)

## Правило по путям + как проверили
Вложенный `src/main/java/ru/janeforjane/timeline/llm/AGENTS.md` действует только в модуле `llm/`.
Проверка: задача в `llm/` → агент сослался на `llm/AGENTS.md` (таймаут, fallback, structured output);
задача в `controller/` → этих правил не видел, руководствовался только глобальным `AGENTS.md`
(и отказался класть бизнес-логику в контроллер).
Скрины: `screenshots/embedded-agents-1.png`, `embedded-agents-2.png`, `embedded-agents-3.png`.

![embedded-agents-1.png](screenshots/embedded-agents-1.png)

![embedded-agents-2.png](screenshots/embedded-agents-2.png)

![embedded-agents-3.png](screenshots/embedded-agents-3.png)

## Навыки (по одной строке)
- `write-tests` (скилл, авто) — процедура написания JUnit-тестов по конвенции `should_..._when_...`.
- `/wrong-branch` (команда, ручная) — перенос незакоммиченных изменений с текущей ветки в новую.

## Скриншоты
1. Блокировка `.env` — `screenshots/permission-env-deny-1.png`, `permission-env-deny-2.png`.

![permission-env-deny-1.png](screenshots/permission-env-deny-1.png)

![permission-env-deny-2.png](screenshots/permission-env-deny-2.png)

2. Срабатывание линтера — `screenshots/linter-hook-works-1.png`, `linter-hook-works-2.png`.

![linter-hook-works-1.png](screenshots/linter-hook-works-1.png)

![linter-hook-works-2.png](screenshots/linter-hook-works-2.png)

3. Продолжение по памяти — `screenshots/mem-1.png`.

![mem-1.png](screenshots/mem-1.png)

4. Зелёный e2e-тест — `screenshots/e2e-green.png`.

![playwright-todo.png](screenshots/playwright-todo.png)

![e2e-green-1.png](screenshots/e2e-green-1.png)

![e2e-green-2.png](screenshots/e2e-green-2.png)


---

# ДЗ#6 — агентно-ориентированный пайплайн (GSD)

## Что сделано (по 5 требованиям)

1. **GSD + документы проекта** — установлен `@opengsd/gsd-core` (локально, `--opencode`), проект инициализирован.
2. **Память** — `STATE.md`/`CONTEXT.md` (GSD) + handoff `docs/plan.md` / `docs/memory.md`.
3. **Команды + hooks** — `/gsd-*`, плагин `gsd-core.js`, хуки GSD (+ свой `project-guard.ts`).
4. **Управление контекстом** — compaction + usage до/после.
5. **Discovery пройден** — требования и roadmap (`.planning/`).

## 1. GSD 

**Шаг 0 — установка.** Из корня проекта:
```bash
npx @opengsd/gsd-core@latest --opencode --local
```
Перезапустить opencode → /gsd-help (появились команды /gsd-*).
команды /gsd-*

**Шаг 1 — карта кода.** /gsd-map-codebase → .planning/codebase/ (7 файлов: STACK, ARCHITECTURE, STRUCTURE, CONVENTIONS, TESTING, INTEGRATIONS, CONCERNS).
codebase map
![gsd-codebase-map.png](screenshots/gsd-codebase-map.png)

**Шаг 2 — настройка.** /gsd-settings → профиль моделей inherit (DeepSeek), TDD On, Auto-Advance Off, коммит .planning/ On, остальное дефолт.
настройки GSD
![gsd-settings.png](screenshots/gsd-settings.png)

**Шаг 3 — инициализация проекта.** /gsd-new-project → вопросы (Полный MVP, PostgreSQL сразу, вертикальный MVP) → созданы PROJECT.md, REQUIREMENTS.md (16 требований v1), ROADMAP.md (4 фазы), STATE.md.
артефакты / ROADMAP
![gsd-roadmap.png](screenshots/gsd-roadmap.png)

**Проверка результата.** 
mvn test и mvn checkstyle:check зелёные; 
.planning/config.json — inherit, auto_advance: false, tdd: true.

## 2. Память + чекпойнты

Память агента реализована файлами, два механизма.

    Основной источник состояния GSD-фаз — STATE.md (пишет движок). 
    docs/memory.md/docs/plan.md — handoff для не-GSD задач и заметок.

### Механизм 1 — память GSD (детерминированная)
- Файлы: `.planning/STATE.md`, `ROADMAP.md`, `PROJECT.md`.
- Пишет: движок GSD (`gsd-tools`), НЕ модель.
- Триггер: запуск `/gsd-*` команд + хуки на события сессии (SessionStart, phase-boundary, graphify).


### Механизм 2 — handoff (по правилу AGENTS.md)
- Файлы: `docs/plan.md`, `docs/memory.md` (из ДЗ#3).
- Пишет: сама модель (LLM).
- Триггер: правило в `AGENTS.md` — «после каждой завершённой задачи отметь чекбокс и допиши memory.md».
- Наблюдение: за ДЗ#6 эти файлы не менялись — GSD пишет в свой `STATE.md`, а не сюда; 
   чтобы handoff обновился, агента нужно попросить явно:

    ![forced-memory-1.png](screenshots/forced-memory-1.png)

    ![forced-memory-2.png](screenshots/forced-memory-2.png)

### Демонстрация «сохранение → извлечение»
1. `/gsd-pause-work` — сохраняет состояние (чекпойнт).

    ![gsd-pause-work.png](screenshots/gsd-pause-work.png)

2. Новая сессия → `/gsd-resume-work` — восстанавливается с того же места.

   ![gsd-resume.png](screenshots/gsd-resume.png)


## 3. Быстрые команды + hooks

- Команды `/gsd-*` (установлены) + своя `/wrong-branch` (из ДЗ#3).
- Hooks: хуки GSD + свой плагин `project-guard.ts`.

## 4. Управление контекстом — автосжатие

- **Автосжатие** — встроенный compaction в opencode, включён по умолчанию.
  срабатывает автоматически, при приближении к лимиту.
  ![compaction-default-settings.png](screenshots/compaction-default-settings.png)
- **Принудительное сжатие (слэш-команда)** - встроенная команда compaction в opencode. 
   Инициирует сжатие контекста.
  
 Usage до и после compaction:

  ![usage-before-compact.png](screenshots/usage-before-compact.png)

![compaction.png](screenshots/compaction.png)

  ![usage-after-compact.png](screenshots/usage-after-compact.png)
