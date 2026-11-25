# Requirements: Timeline Project

**Defined:** 2026-11-25
**Core Value:** Пользователь вводит решение и получает наглядную карту временной линии — LLM-генерация веток и TVA-визуализация равнозначны.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Input

- [ ] **INP-01**: Пользователь вводит текст решения и запускает построение линии кнопкой
- [ ] **INP-02**: Пустой ввод валидируется и не отправляется

### LLM

- [ ] **LLM-01**: LLM возвращает 2 ветки-последствия на каждый узел в формате `{branches: [{consequence, next_dilemma?}]}`
- [ ] **LLM-02**: Каждый внешний вызов LLM выполняется с таймаутом
- [ ] **LLM-03**: Ответ LLM парсится в DTO через structured output, а не как сырая JSON-строка
- [ ] **LLM-04**: При недоступности LLM используются шаблонные ветки по правилам (fallback)
- [ ] **LLM-05**: Вызовы LLM кэшируются по хэшу (узел + уровень)
- [ ] **LLM-06**: Действует лимит LLM-вызовов на сессию

### Graph

- [ ] **GRPH-01**: Пользователь видит дерево глубиной 3 и шириной 2 (корень → 2 → 4 → 8 финалов)
- [ ] **GRPH-02**: Клик по узлу открывает панель с текстом последствия и кнопкой «Продолжить эту ветку»
- [ ] **GRPH-03**: На уровне 3 вместо следующей развилки отображается итог (`final_summary`)

### Map

- [ ] **MAP-01**: Пользователь получает карту линии — полный сохранённый граф
- [ ] **MAP-02**: На карте отображается итог по веткам (плюсы/минусы)

### History

- [ ] **HIST-01**: Пользователь видит список сохранённых линий
- [ ] **HIST-02**: Сохранённую линию можно открыть повторно

### Persistence

- [ ] **PERS-01**: Данные хранятся в PostgreSQL (таблицы `decision`, `node`, `edge`)
- [ ] **PERS-02**: Дерево и карта восстанавливаются из БД после перезагрузки

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Deploy

- **DEPL-01**: Деплой на российский PaaS для JVM-приложений (Amvera или аналогичный) как fat jar без Docker — финальный выбор площадки на этапе деплоя

### Export

- **EXPR-01**: Экспорт карты линии как изображение

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| WebSocket / реальное время | Это инструмент, а не игра |
| React / тяжёлый фронтенд | Достаточно минимального JS / Thymeleaf-HTML |
| Docker | Не обязателен — деплой как fat jar |
| Авторизация / многопользовательность | Single-user локальный инструмент |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|

**Coverage:**
- v1 requirements: 16 total
- Mapped to phases: 0
- Unmapped: 16 ⚠️

---
*Requirements defined: 2026-11-25*
*Last updated: 2026-11-25 after initial definition*
