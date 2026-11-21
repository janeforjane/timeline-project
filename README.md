## Как агент работает в проекте

- **Инструкции:** глобальный `AGENTS.md` (стек, команды, конвенции, запреты) + вложенные
  `AGENTS.md` для модулей (например `llm/`).
- **Навыки:** `write-tests` (вызывается сам при задаче на тесты), `/wrong-branch` (запуск вручную).
- **Права** (`opencode.json`): allow — `mvn test/compile/checkstyle:check/clean`, `git commit` и др.;
  ask — `git push` и всё прочее; deny — чтение `.env`, `rm -rf`. Режим по умолчанию — ask-before-edit
  (каждая правка требует подтверждения — сохраняю контроль над изменениями).
- **Проверки (страхуют агента):** Spotless (форматтер после каждой правки, плагин `project-guard.ts`),
  Checkstyle (линтер перед коммитом), блок правок/чтения защищённых файлов (`.env`, `.git/`,
  `application-local.yml`, `target/`).
- **Память между сессиями:** handoff-файлы `docs/plan.md` + `docs/memory.md`.
- **Запуск:** `mvn spring-boot:run` · тесты `mvn test` · линтер `mvn checkstyle:check` ·
  форматтер `mvn spotless:apply`.