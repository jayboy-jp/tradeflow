# Tradeflow API

Professional-style trading REST API (similar to Zerodha/Kite) built with Spring Boot.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Security + JWT
- Spring Data JPA (PostgreSQL)
- Redis (ready for caching phase)
- Swagger/OpenAPI
- Docker + Docker Compose

## Project Profiles

- `dev` profile: local development defaults (`localhost`, SQL logs on)
- `prod` profile: values from environment variables

Base config is in `application.properties`, and profile-specific config is in:

- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`

## Local Run (without Docker)

1. Ensure PostgreSQL is running.
2. Set env vars (or rely on dev defaults).
3. Run:

```bash
./mvnw spring-boot:run
```

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Docker Run (Phase 1)

1. Copy env template:

```bash
cp .env.example .env
```

2. Start all services (app + postgres + redis):

```bash
docker compose up --build
```

3. Stop:

```bash
docker compose down
```

4. Stop and remove volumes (fresh DB):

```bash
docker compose down -v
```

## Environment Variables

Defined in `.env.example`:

- `SPRING_PROFILES_ACTIVE`
- `APP_PORT`
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`
- `REDIS_PORT`
- `JWT_SECRET`, `JWT_EXPIRATION_MS`

Do not commit real secrets (`.env` is gitignored).
