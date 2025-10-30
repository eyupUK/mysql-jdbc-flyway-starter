# MySQL + JDBC + Flyway Starter

A tiny, production‑style starter that lets you **design a MySQL schema**, **migrate with Flyway**, **seed with DataFactory/Faker**, and **test with raw JDBC** (plus Testcontainers).

## What you get
- **MySQL schema**: `customers`, `products`, `orders`, `order_items`
- **Migrations**: `src/main/resources/db/migration/V1__initial_schema.sql`
- **Seeding**: Java `Seeder` uses DataFactory + Faker
- **JDBC DAO**: raw JDBC `CustomerDao`
- **Tests**: JUnit 5 + Testcontainers spins up MySQL, runs Flyway, and verifies CRUD
- **Docker**: `docker-compose.yml` for local MySQL
- **Flyway plugin**: `mvn -Pdev flyway:migrate`

## Quick start (Local MySQL)
```bash
# 1) Start MySQL locally
docker compose up -d mysql

# 2) Run migrations (Flyway)
./mvnw -Pdev -Denv.DB_URL="jdbc:mysql://localhost:3306/shopdb?useSSL=false&allowPublicKeyRetrieval=true"        -Denv.DB_USER=shop -Denv.DB_PASS=shop_pw flyway:migrate

# 3) Seed sample data (20 by default; override with SEED_COUNT)
SEED_COUNT=100 ./mvnw -Pdev -Dexec.cleanupDaemonThreads=false exec:java
```

## Run tests (no local MySQL required)
```bash
./mvnw -q -DskipTests=false test
```

This uses **Testcontainers** to launch MySQL in Docker, applies Flyway migrations, then does JDBC CRUD.

## Config
- Env vars or system props:
  - `DB_URL` (e.g. `jdbc:mysql://localhost:3306/shopdb?useSSL=false&allowPublicKeyRetrieval=true`)
  - `DB_USER`, `DB_PASS`
- Defaults for dev are in the Maven `dev` profile and `application.properties`

## Exploring JDBC
See `src/main/java/com/example/app/dao/CustomerDao.java` for a small DAO with prepared statements and generated keys.

## Extending
- Add new migrations as `V2__*.sql`, `V3__*.sql`, etc.
- Add seeders per table or write SQL seed files and run them with Flyway callbacks.
- Wrap DAOs with a service layer or swap in JPA later—this starter stays framework‑agnostic.

---

Happy testing!
