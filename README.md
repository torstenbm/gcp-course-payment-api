# Payment Core API

A Spring Boot service for managing payments. Exposes a REST API for creating, listing, and advancing payments. Persists to Cloud SQL (PostgreSQL) and publishes events to Google Cloud Pub/Sub. Designed to run on Cloud Run.

## Tech Stack

- **Java 21** / **Spring Boot 3.5**
- **Spring Data JPA** + **PostgreSQL** (Cloud SQL in production, H2 in tests)
- **Liquibase** for database migrations
- **Google Cloud Pub/Sub** for event publishing
- **SpringDoc OpenAPI** for API documentation

## Project Structure

```
src/main/java/com/impact/paymentcore/
├── controller/         REST endpoints
├── dto/                Request/response DTOs
├── exception/          Custom exceptions with HTTP status mapping
├── model/              JPA entities and enums
├── repository/         Spring Data repositories
└── service/            Business logic
```

## API Endpoints

| Method | Path                      | Description                          | Status  |
|--------|---------------------------|--------------------------------------|---------|
| GET    | `/payments`               | List all payments                    | 200     |
| POST   | `/payments`               | Create a new PENDING payment         | 201     |
| POST   | `/payments/{id}/advance`  | Advance a PENDING payment to ADVANCED| 200     |

Swagger UI is available at `/swagger-ui.html` when the app is running.

## Running Locally

### Prerequisites

- Java 21
- A GCP project with Cloud SQL (PostgreSQL) and Pub/Sub provisioned
- `gcloud` CLI authenticated

### Authenticate

```bash
gcloud auth application-default login
```

### Run

```bash
GOOGLE_CLOUD_PROJECT=your-project \
CLOUD_SQL_INSTANCE=your-project:europe-north1:your-instance \
DB_NAME=payments \
DB_USER=postgres \
DB_PASSWORD=secret \
./gradlew bootRun
```

The app starts on `http://localhost:8080`.

## Running Tests

Tests use an in-memory H2 database and mock Pub/Sub — no GCP credentials needed.

```bash
./gradlew test
```

## Database Migrations

Schema is managed by Liquibase. Migrations live in `src/main/resources/db/changelog/migrations/` and are applied automatically on startup.

To add a new migration, create a file in that directory and include it in `db.changelog-master.yaml`.

## Deploying to Cloud Run

### GCP Prerequisites

1. **Cloud SQL** — A PostgreSQL instance with a `payments` database created
2. **Pub/Sub** — A topic named `new-payments-topic`
3. **Service account** — The Cloud Run service account needs:
   - `Cloud SQL Client`
   - `Pub/Sub Publisher`

### Build and Push

```bash
gcloud builds submit --tag gcr.io/YOUR_PROJECT/payment-core-api
```

### Deploy

```bash
gcloud run deploy payment-core-api \
  --image gcr.io/YOUR_PROJECT/payment-core-api \
  --region europe-north1 \
  --add-cloudsql-instances YOUR_PROJECT:europe-north1:YOUR_INSTANCE \
  --set-env-vars "\
GOOGLE_CLOUD_PROJECT=YOUR_PROJECT,\
CLOUD_SQL_INSTANCE=YOUR_PROJECT:europe-north1:YOUR_INSTANCE,\
DB_NAME=payments,\
DB_USER=postgres,\
DB_PASSWORD=secret"
```

### Environment Variables

| Variable              | Description                                          | Default                                     |
|-----------------------|------------------------------------------------------|---------------------------------------------|
| `GOOGLE_CLOUD_PROJECT`| GCP project ID                                      | —                                           |
| `CLOUD_SQL_INSTANCE`  | Cloud SQL instance connection name                   | —                                           |
| `DB_NAME`             | Database name                                        | `payments`                                  |
| `DB_USER`             | Database user                                        | `postgres`                                  |
| `DB_PASSWORD`         | Database password                                    | —                                           |

Liquibase runs on startup and applies any pending migrations automatically, so the schema is always up to date after a deploy.
