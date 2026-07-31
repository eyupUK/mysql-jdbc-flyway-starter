# Runtime Guide

This project is intentionally framework-free. Maven builds and launches the application, Flyway controls the schema, JDBC performs database access, and Testcontainers provides an isolated MySQL database for tests. This keeps the runtime behavior explicit and easy to inspect.

## Runtime Model

```text
Maven command
  |
  +-- flyway:migrate -> Flyway Maven plugin -> MySQL schema migrations
  |
  +-- exec:java -> Main -> Env -> Flyway -> Seeder -> JDBC query
  |
  +-- test -> JUnit -> Testcontainers MySQL -> Flyway -> JDBC assertions
  |
  +-- -Pmask exec:java -> DataMasker -> JDBC update of customer PII
```


## Configuration

`Env` resolves each database setting independently in this order:

1. Environment variable: `DB_URL`, `DB_USER`, or `DB_PASS`
2. Java system property with the same name
3. `src/main/resources/application.properties`

This means an environment variable always wins, and explicit `-DDB_*` properties are never overwritten by local defaults.

For example, to run the main application against a specific database:

```bash
mvn \
  -DDB_URL="jdbc:mysql://localhost:3306/shopdb?useSSL=false&allowPublicKeyRetrieval=true" \
  -DDB_USER=shop \
  -DDB_PASS=shop_pw \
  exec:java
```

The `dev` Maven profile supplies values named `env.DB_URL`, `env.DB_USER`, and `env.DB_PASS` to the Flyway Maven plugin. Those properties are specific to the `flyway:*` commands; application code continues to use the `DB_*` values above.

## Local Database Flow

Start MySQL with Docker Compose:

```bash
docker compose up -d mysql
```

Apply the versioned migrations:

```bash
mvn -Pdev flyway:migrate
```

Run the application entry point:

```bash
SEED_COUNT=20 mvn -Pdev exec:java
```

`Main` runs the following sequence:

1. Loads database defaults through `Env`.
2. Configures Flyway with the resolved database connection and applies outstanding migrations.
3. Reads `SEED_COUNT`, using `20` when it is absent.
4. Calls `Seeder.seed(n, n * 3, n * 5)` to create customers, products, orders, and order items in one transaction.


## Why Flyway Runs First

Flyway runs before seeding or DAO work because application code depends on tables and foreign keys being present. Migrations are versioned SQL files such as `V1__initial_schema.sql`. Flyway records applied versions in `flyway_schema_history`, so a database receives each migration once and can be moved forward predictably as the schema changes.

Add schema changes as a new migration, for example `V2__add_customer_phone.sql`; do not edit an already-applied migration in a shared environment.

## JDBC Access


- Prepared statements bind input values safely.
- `RETURN_GENERATED_KEYS` retrieves the identifier from an insert.
- Try-with-resources closes every JDBC resource.
- Queries return only the data the caller needs.


## Data Masking Flow

The `mask` profile replaces the normal `Main` entry point with `DataMasker`:

```bash
mvn -Pmask -Dexec.args="--confirm" exec:java
```

`DataMasker` refuses to run without the exact `--confirm` argument. With confirmation, it replaces every customer email, first name, and last name with deterministic values based on the customer ID, such as `masked+42@example.invalid` and `Customer 42`.

The update is idempotent, so running it again leaves already masked rows unchanged. It is irreversible and must only be used on a non-production database copy. It does not run during migrations, normal application execution, or tests unless a test calls it directly.

## Test Flow

Run all tests with:

```bash
mvn -q -DskipTests=false test
```

No local MySQL server is used. The test lifecycle is:

1. Testcontainers connects to Docker and starts a disposable MySQL 8.4 container.
2. Flyway applies the migrations to that container.
3. The test setup sets `DB_URL`, `DB_USER`, and `DB_PASS` system properties so production JDBC code talks to the container.
6. Testcontainers removes the disposable container when the JVM exits.

This split gives fast coverage for deterministic logic while retaining real-database coverage for SQL, migrations, and JDBC behavior.

## Common Commands

```bash
# Compile and run all tests
mvn test

# Apply migrations to the local Compose database
mvn -Pdev flyway:migrate

# Seed the local database through Main
SEED_COUNT=100 mvn -Pdev exec:java

# Mask a non-production database explicitly
mvn -Pmask -Dexec.args="--confirm" exec:java
```

Use `./mvnw` in place of `mvn` on a machine without a global Maven installation.

## Troubleshooting

- `mvn: command not found`: use `./mvnw test` or install Maven 3.9 or newer.
- Testcontainers cannot find Docker: start Docker Desktop or Docker Engine and confirm `docker info` succeeds.
- A local migration cannot connect: verify the Compose container is running and that `DB_URL`, `DB_USER`, and `DB_PASS` match its configuration.
- The mask command fails immediately: pass the exact argument `-Dexec.args="--confirm"`; this guard is intentional.
