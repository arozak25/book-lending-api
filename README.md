# Book Lending API

This is a Spring Boot REST API for managing books, members, and loans in a library.

## Features

- Manage books (`/books`)
- Manage members (`/members`)
- Create and complete loans (`/loans`)
- Loan business rules:
  - Maximum active loans per member (configurable)
  - Loan in the defined max duration days (configurable)
  - Prevent borrowing when a member has overdue active loans
  - Prevent borrowing when no copies are available
- Flyway-based schema migration
- MySQL for the database
- Integration tests with Spring Boot Test + MockMvc
- API documentation with Spring REST Docs

## Tech Stack

- Java 17
- Spring Boot 4.0.2
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Flyway
- MySQL 8.4
- Maven Wrapper

## Prerequisites

- Java 17+
- Docker + Docker Compose

## Environment Variables

The application reads database and loan rule settings from environment variables.

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `DB_HOST` | Yes (local run) | - | MySQL host and port, e.g. `localhost:3306` |
| `DB_NAME` | Yes (local run) | - | Database name |
| `DB_USERNAME` | Yes (local run) | - | Database username |
| `DB_PASSWORD` | Yes (local run) | - | Database password |
| `LOAN_MAX_ACTIVE_PER_MEMBER` | No | `3` | Max active loans per member |
| `LOAN_DURATION_DAYS` | No | `14` | Loan due date duration in days |
| `JWT_SECRET` | No | `replace-this-with-a-32-byte-minimum-jwt-secret` | HMAC secret used to sign JWTs |
| `JWT_EXPIRATION_MINUTES` | No | `60` | JWT validity duration in minutes |
| `APP_PORT` | No (Docker Compose) | `8080` | Host port mapped to API container |
| `DB_PORT` | No (Docker Compose) | `3306` | Host port mapped to MySQL container |

## Run with Docker Compose

From project root:

```bash
make up
make logs
```

API will be available on `http://localhost:8080` by default.

To stop:

```bash
make down
```

To remove volumes too:

```bash
make clean
```

## Run Locally (Without API Container)

1. Start MySQL:

```bash
docker compose up -d mysql
```

2. Export environment variables:

```bash
export DB_HOST=localhost:3306
export DB_NAME=book_lending
export DB_USERNAME=book_lending
export DB_PASSWORD=book_lending
```

3. Run app:

```bash
make run
```

## Build, Test, Lint

```bash
make build
make test
make lint
```

Notes:
- `make test` will ensures test MySQL infra is running via `scripts/ensure-test-infra.sh`.
- `make lint` runs `spotless:apply` (it formats source files).

## Authentication and Authorization

- Spring Security is enabled.
- `POST /auth/token` is public and returns a JWT.
- All other API endpoints require a valid bearer token for user role `ADMIN`.
- Default in-memory admin user:
  - Username: `admin`
  - Password: `Admin123!`
- Use the returned token in request headers: `Authorization: Bearer <token>`

## API Documentation

This project uses Spring REST Docs with MockMvc tests as the source of truth for API docs.

Generate snippets from tests:

```bash
./mvnw -Dtest=AuthControllerIntegrationTest,BookControllerIntegrationTest,MemberControllerIntegrationTest,LoanControllerIntegrationTest test
```

Generate the rendered documentation from generated snippets:

```bash
./mvnw -DskipTests prepare-package
```

Or run both in one command:

```bash
./mvnw -Dtest=AuthControllerIntegrationTest,BookControllerIntegrationTest,MemberControllerIntegrationTest,LoanControllerIntegrationTest test prepare-package
```

Generated files:
- Snippets: `target/generated-snippets`
- Rendered HTML: `target/generated-docs/index.html`
- AsciiDoc source: `src/docs/asciidoc/index.adoc`

## Database

- Flyway migration scripts are in `src/main/resources/db/migration`.
- Initial schema is defined in `V1__baseline.sql`.

## Actuator

Spring Boot Actuator is enabled for operational visibility and basic runtime monitoring.

- Base path: `/actuator`
- Exposed endpoints:
    - `/actuator/health`
    - `/actuator/info`
    - `/actuator/metrics`
- Current security setup permits actuator endpoints without authentication.
- Health endpoint is configured with `show-details: always`.
