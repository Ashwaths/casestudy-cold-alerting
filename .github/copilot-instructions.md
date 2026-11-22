# Copilot instructions for Nimbus Freight

## Project overview

Nimbus Freight is a cold-chain monitoring case study with an Angular dashboard
and one Spring Boot deployable. Do not introduce or document it as a microservices
system.

- Java 25 and Spring Boot 3.5.16
- Angular 22.3 and TypeScript 6
- Maven reactor root: `pom.xml`
- Backend: `backend/temperature-analysis-service`
- Frontend: `frontend`
- Database migrations: `backend/temperature-analysis-service/src/main/resources/db/migration`
- Runtime deployment: `Dockerfile`, `docker-compose.yml`, `frontend/nginx.conf`
- Monitoring: `monitoring/prometheus/prometheus.yml` and Grafana provisioning

## Architecture and ownership

- Keep API contracts, ingestion, temperature analysis, incident management,
  truck management, repositories, and notification adapters in the existing
  single Spring Boot module.
- Put REST endpoints in `com.nimbusfreight.temperature.api`, domain behavior in
  `com.nimbusfreight.temperature.service`, persistence entities in `model`,
  and Spring Data repositories in `repo`.
- The Angular dashboard is under `frontend/src/app`. Reuse `api.service.ts`,
  existing models, core auth services, and the auth interceptor for API calls.
- Flyway migration files are the canonical database schema. Create a new
  versioned migration for schema changes; never change an already-applied
  migration.
- Nginx is the only public edge in Compose. Do not publish API or database
  ports in `docker-compose.yml`.

## Domain rules

- The default temperature threshold is 8 C.
- An incident opens after 15 minutes of readings continuously above the
  configured threshold.
- An incident resolves after 10 minutes continuously at or below the threshold.
- Missing telemetry is not safe telemetry: a reading gap longer than the
  configured data-gap duration (10 minutes by default) breaks continuity.
- Evaluate readings by event timestamp, not arrival time.
- Emit one `INCIDENT_OPENED` and one `INCIDENT_RESOLVED` lifecycle record for
  each incident.
- Preserve authenticated sensor ingestion and current CSRF/session behavior.
  Authentication is Microsoft Entra ID. Authorization currently requires only
  an authenticated user; do not add role assumptions unless explicitly requested.

## Engineering conventions

- Follow existing package, naming, constructor, repository, validation, and test
  patterns; make the smallest complete change.
- Add or update focused tests for changed behavior. Prefer existing JUnit and
  Spring test conventions under `src/test/java`.
- Use Jakarta validation for API request fields and keep exception responses
  compatible with `ApiExceptionHandler`.
- Prefer constructor injection and keep services transactional only when writes
  or lifecycle consistency require it.
- Preserve event-time semantics and avoid introducing hard-coded time or
  threshold values outside configuration.
- Do not add secrets, certificate private keys, credentials, or environment
  values to source control. `.env.example` and Key Vault describe secret
  configuration; runtime secret names include `AZURE-CLIENT-SECRET` and
  `ALERT-WEBHOOK-BEARER-TOKEN`.
- Do not edit generated output such as `target/`, `frontend/dist/`,
  `bin/`, or checked-in certificates.

## Required validation

For backend or full-stack changes, run:

```sh
mvn verify
```

For frontend changes, run:

```sh
cd frontend
npm ci
npm run build
```

For Compose or deployment changes, also run `docker compose config` and report
any environment variables that cannot be resolved locally.

Before finishing, inspect the final diff for accidental secrets, generated
files, unrelated changes, and migration edits.
