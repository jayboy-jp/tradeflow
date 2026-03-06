# Tradeflow API

A professional-style trading REST API (similar to Zerodha/Kite) built with Spring Boot. Supports user registration, JWT auth, wallet, stocks, portfolio, orders, and trade history.

## Tech Stack

- **Java 21** · **Spring Boot 4** · **Spring Security** · **JPA** · **PostgreSQL**
- **JWT** for authentication
- **Swagger/OpenAPI 3** at `/swagger-ui.html`

## Quick Start

1. **PostgreSQL**: Create database `tradeflow` and set credentials in `application.properties`.
2. **Run**: `./mvnw spring-boot:run`
3. **Docs**: Open http://localhost:8080/swagger-ui.html
4. **Health**: GET http://localhost:8080/health

## API Overview

### Public (no auth)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Register (email, password) |
| POST | `/api/v1/auth/login` | Login → returns JWT |
| GET | `/health` | Health check |

### Protected (Header: `Authorization: Bearer <token>`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/users/me` | Current user profile + balance |
| POST | `/api/v1/users/me/fund` | Add funds to wallet |
| GET | `/api/v1/wallet` | Wallet (available, locked, total) |
| GET | `/api/v1/stocks` | List all stocks |
| GET | `/api/v1/stocks/{id}` | Get stock by ID |
| GET | `/api/v1/stocks/symbol/{symbol}` | Get stock by symbol |
| POST | `/api/v1/stocks` | Create stock (body: symbol, name, price) |
| GET | `/api/v1/portfolio` | Current holdings |
| POST | `/api/v1/orders` | Place order (body: stockId, type, price, quantity) |
| GET | `/api/v1/orders` | List orders (page, size, status?) |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| POST | `/api/v1/orders/{id}/execute` | Execute pending order |
| POST | `/api/v1/orders/{id}/cancel` | Cancel pending order |
| GET | `/api/v1/trades` | Trade history (filled orders) |

## Order Flow

1. **Place order** (BUY/SELL) → funds/shares validated; for BUY, amount is **locked**.
2. **Execute** → settles: deducts cash and adds shares (BUY) or removes shares and adds cash (SELL).
3. **Cancel** → only for PENDING; releases locked funds (BUY).

## Configuration

- **JWT**: Set `app.jwt.secret` (min 256 bits) or env `JWT_SECRET`. Default expiration 24h.
- **DB**: `spring.datasource.*` in `application.properties`.
