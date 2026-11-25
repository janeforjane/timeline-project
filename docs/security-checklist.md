# Security Checklist

Чек-лист безопасности проекта timeline. Обновляется вручную по мере развития.

## Секреты
- [x] `.env` — в `.gitignore`, не коммитится
- [x] `application-local.yml` — в `.gitignore`
- [x] Чтение `.env` агенту запрещено (permissions `read .env → deny`)
- [ ] API-ключ LLM — только в `.env` / env-переменных, не в коде и не в git

## Права агента (opencode.json)
- [x] `edit: ask` — каждая правка требует подтверждения
- [x] `git push` → ask
- [x] `rm -rf` / `rm -r` → deny
- [x] чтение `.env` → deny

## Хуки / авто-защита
- [x] GSD guard-хуки: prompt-guard, secret-read-guard, read-injection-scanner
- [x] Свой плагин `project-guard.ts`: блок правок `.env` / `.git/` / `application-local.yml` / `target/`
- [x] Spotless (форматтер после правки) + Checkstyle (линтер перед коммитом)

## Git
- [x] `.gitignore`: `target/`, `.idea/`, `.env`, `application-local.yml`, движок GSD
- [x] Ветки по ДЗ (`hw/NN-...`), мерж в `master` после сдачи

## На будущее (ДЗ#18)
- [ ] OWASP-проверка кода
- [ ] Аудит зависимостей (`mvn dependency-check`)
- [ ] HTTP-заголовки безопасности при деплое