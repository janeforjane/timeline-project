---
name: write-tests
description: Use when writing or fixing unit tests in this Java project. Triggers on "напиши тест", "покрой тестами", "write test", JUnit, or when a class needs coverage.
---

# write-tests — как писать тесты в этом проекте

## Правила
- JUnit 5, имена вида `should_<результат>_when_<условие>` (пример: `shouldReturnBranches_whenDecisionValid`).
- Тесты класть в `src/test/java/ru/janeforjane/timeline/<тот же под-пакет>`.
- Один тест — одно поведение. Покрывать и happy path, и границы/ошибки.
- Только assertion-ы, никаких System.out.

## Порядок
1. Найди класс и его зависимости (constructor injection).
2. Напиши тест через Arrange / Act / Assert.
3. Запусти `mvn test -Dtest=ИмяКласса`, доведи до зелёного.
4. Обнови `docs/memory.md`.