# Candle Aggregation Service

## Project Overview

Candle Aggregation Service is a backend system built using **Spring Boot 4**, **Java 21**, and **PostgreSQL** that:

- Ingests real-time bid/ask market data
- Aggregates events into OHLC (Open, High, Low, Close) candles
- Supports multiple symbols and time intervals
- Stores candle data efficiently in PostgreSQL
- Exposes historical candle data through a REST API
- Provides Prometheus metrics for observability

The service simulates market data using a scheduled task and aggregates candles per:

- Symbol (e.g., BTC-USD, ETH-USD)
- Interval (1s, 5s, 1m, 15m, 1h)

Historical data is exposed in a format compatible with charting libraries such as TradingView Lightweight Charts.

---

## Tech Stack

### Backend
- Java 21
- Spring Boot 4.0.3
- Spring Web (REST API)
- Spring Data JPA
- Hibernate ORM

### Database
- PostgreSQL
- H2 (for test profile)

### Concurrency & Performance
- HikariCP Connection Pool
- Atomic PostgreSQL `ON CONFLICT DO UPDATE`
- Composite Indexing

### Observability
- Spring Boot Actuator
- Micrometer
- Prometheus Metrics Endpoint

### Testing
- JUnit 5
- Mockito
- Spring Boot Test

### Build Tool
- Maven

---

## Architecture Summary=
