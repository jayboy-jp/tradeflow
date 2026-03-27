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
- `REFRESH_TOKEN_EXPIRATION_MS`

Do not commit real secrets (`.env` is gitignored).

## Phase 2 Security Hardening

- Refresh-token based auth lifecycle:
  - `POST /api/v1/auth/login` returns `accessToken` + `refreshToken`
  - `POST /api/v1/auth/refresh` rotates refresh token and issues a new access token
  - `POST /api/v1/auth/logout` revokes refresh token
- Basic rate limiting on `/api/v1/auth/**` endpoints (per IP, Redis-backed window).

## Phase 3 (Redis-backed)

- Distributed auth rate limiting using Redis counters:
  - `AUTH_RATE_LIMIT_MAX_REQUESTS`
  - `AUTH_RATE_LIMIT_WINDOW_SECONDS`
- Redis caching for stock read endpoints:
  - `GET /api/v1/stocks`
  - `GET /api/v1/stocks/{id}`
  - `GET /api/v1/stocks/symbol/{symbol}`
- Cache invalidation on stock creation.

## Phase 4 (Real-time)

- Server-sent events (SSE) for order status updates.
- Endpoint (authenticated):
  - `GET /api/v1/stream/orders`

When you call:
- `POST /api/v1/orders` (place)
- `POST /api/v1/orders/{id}/execute` (execute)
- `POST /api/v1/orders/{id}/cancel` (cancel)

the server pushes `order` events to your SSE stream.
