# MySQL + JDBC + Flyway Starter

[![CI](https://github.com/eyupUK/mysql-jdbc-flyway-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/eyupUK/mysql-jdbc-flyway-starter/actions/workflows/ci.yml)

A tiny, production‑style starter that lets you **design a MySQL schema**, **migrate with Flyway**, **seed with DataFactory/Faker**, and **test with raw JDBC** (plus Testcontainers).

See [GUIDE.md](GUIDE.md) for the runtime architecture, configuration precedence, and command lifecycles.

## What you get
- **MySQL schema**: `customers`, `products`, `orders`, `order_items`
- **Migrations**: `src/main/resources/db/migration/V1__initial_schema.sql`
- **Seeding**: Java `Seeder` uses DataFactory + Faker
- **Data masking**: irreversible, idempotent customer PII masking for non-production copies
- **JDBC DAO**: raw JDBC `CustomerDao`
- **Tests**: JUnit 5 + Testcontainers spins up MySQL, runs Flyway, and verifies CRUD and seeding
- **Docker**: `docker-compose.yml` for local MySQL
- **Flyway plugin**: `mvn -Pdev flyway:migrate`

## Requirements
- JDK 21 or newer
- Docker Desktop or Docker Engine, running and accessible from the command line
- Apache Maven 3.9 or newer, available as `mvn`

## Quick start (Local MySQL)
```bash
# 1) Start MySQL locally
docker compose up -d mysql

# 2) Run migrations (Flyway)
mvn -Pdev \
  -Denv.DB_URL="jdbc:mysql://localhost:3306/shopdb?useSSL=false&allowPublicKeyRetrieval=true" \
  -Denv.DB_USER=shop \
  -Denv.DB_PASS=shop_pw \
  flyway:migrate

# 3) Seed sample data (20 by default; override with SEED_COUNT)
SEED_COUNT=100 mvn -Pdev -Dexec.cleanupDaemonThreads=false exec:java
```

## Run tests (no local MySQL required)
```bash
mvn -q -DskipTests=false test
```

This uses **Testcontainers** to launch MySQL in Docker, applies Flyway migrations, then does JDBC CRUD.
The bundled Maven Wrapper remains available as `./mvnw` when a global Maven installation is unavailable.

## Continuous Integration
GitHub Actions runs the full Testcontainers test suite on every push and pull request using Java 21. The workflow uses the Maven Wrapper and verifies Docker before starting the tests.

## Config
- Env vars or system props:
  - `DB_URL` (e.g. `jdbc:mysql://localhost:3306/shopdb?useSSL=false&allowPublicKeyRetrieval=true`)
  - `DB_USER`, `DB_PASS`
- Defaults for dev are in the Maven `dev` profile and `application.properties`

## Data masking
`DataMasker` replaces every customer email, first name, and last name with deterministic placeholders. It is intended for a non-production copy only and never runs as part of migrations or seeding.

```bash
mvn -Pmask -Dexec.args="--confirm" exec:java
```

Set `DB_URL`, `DB_USER`, and `DB_PASS` as environment variables or system properties to target a database other than the local defaults. The operation is idempotent: re-running it does not alter already masked rows.

## Exploring JDBC
See `src/main/java/com/example/app/dao/CustomerDao.java` for a small DAO with prepared statements and generated keys.

## Extending
- Add new migrations as `V2__*.sql`, `V3__*.sql`, etc.
- Add seeders per table or write SQL seed files and run them with Flyway callbacks.
- Wrap DAOs with a service layer or swap in JPA later—this starter stays framework‑agnostic.

---

Happy testing!
