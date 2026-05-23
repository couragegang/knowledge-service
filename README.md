# knowledge-service

Knowledge sources and full-text search for workspaces (`/v1/knowledge`).

- **Порт:** 8088
- **Контракт:** в разработке в [`couragegang/api-contracts`](https://github.com/couragegang/api-contracts) (пока описание эндпоинтов ниже)
- **Стек:** Micronaut, PostgreSQL, Flyway, `tsvector` (pgvector — позже)

## API (MVP)

| Метод | Путь | Назначение |
|-------|------|------------|
| `GET` | `/connectors` | Каталог коннекторов (notion, trello, jira, …) |
| `GET` | `/workspaces/{workspaceId}/sources?org_id=` | Список источников |
| `POST` | `/workspaces/{workspaceId}/sources?org_id=` | Создать источник |
| `POST` | `/sources/{sourceId}/reindex` | Stub reindex |
| `POST` | `/search` | Поиск по документам workspace |

## Локальный запуск

```bash
docker compose up --build
```

Полный стек (postgres + 9 сервисов): репозиторий [`couragegang/platform`](https://github.com/couragegang/platform).

## Сборка и покрытие

```bash
./gradlew test jacocoTestCoverageVerification
```

Порог JaCoCo: **branch coverage ≥ 80%** (см. `gradle/jacoco-coverage.gradle.kts`).
