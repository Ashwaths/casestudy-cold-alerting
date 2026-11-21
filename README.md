# Nimbus Freight cold-chain monitor

The repository contains one Spring Boot API module and an Angular operations
dashboard. The API validates and persists truck telemetry, evaluates sustained
temperature excursions, and records incidents and alert history. These
boundaries run in one deployable application, not separate microservices.

The Java application targets Java 25 (LTS) and Spring Boot 3.5.16. The dashboard
uses Angular 22.3.

## Run the full stack with Docker Compose

Requirements: Docker Engine with the Compose plugin. The build pipeline uses
Java 25 and Node.js 26.

1. Copy `.env.example` to `.env` and set the Azure tenant, application/client
   identifiers, Key Vault URI, Azure Database for MySQL hostname/database, and
   managed-identity database principal. The database server, identity grants,
   Key Vault, and Communication Services resource must already exist; this
   repository does not provision Azure infrastructure. Store the Entra
   application secret as `AZURE-CLIENT-SECRET` and any webhook bearer token as
   `ALERT-WEBHOOK-BEARER-TOKEN` in Key Vault. Do not put secret values in `.env`.
   Set `SERVER_NAME` to the hostname in your TLS certificate and provide the
   Grafana administrator password through a deployment secret.
2. Provide a TLS certificate and private key at `certs/fullchain.pem` and
   `certs/privkey.pem`. For local testing, create a self-signed certificate:

   ```sh
   mkdir certs
   openssl req -x509 -nodes -days 30 -newkey rsa:3072 \
     -keyout certs/privkey.pem -out certs/fullchain.pem \
     -subj "/CN=localhost" \
     -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"
   ```

   Use a trusted certificate for a real deployment; browsers will warn for the
   self-signed local certificate.
3. From the repository root, build and start the application:

   ```sh
   docker compose up --build -d
   ```

4. Open `https://localhost`. HTTP traffic is redirected to HTTPS. Sign in with
   Microsoft Entra ID; the dashboard uses a CSRF-protected server session. The
   API and database are not published to the host. The API connects to the
   configured Azure Database for MySQL instance using managed identity, without
   a database password.
5. Check service status and logs:

   ```sh
   docker compose ps
   docker compose logs -f api
   ```

Prometheus is available at `http://localhost:9090` and Grafana at
`http://localhost:3000`. Grafana is provisioned with Prometheus as its default
datasource and uses `GRAFANA_ADMIN_USER` and `GRAFANA_ADMIN_PASSWORD` from
the runtime environment. Sensor ingestion, temperature analysis, incidents, notifications, and
authentication are implemented within the single API module rather than as
separate Docker Compose services.

Flyway initializes the configured Azure database and seeds six demo trucks on
first start. Authentication uses Microsoft Entra ID, a secure session cookie,
and CSRF protection. Authorization still requires an authenticated user; role
mapping is not added by this migration. Never publish the API or database ports
directly.

## Fleet management and alert delivery

The authenticated dashboard refreshes truck telemetry and active incidents
every 15 seconds. Use **Add truck** and **Edit** to manage fleet details.
**Archive** preserves telemetry and incident history while stopping new
readings; **Activate** resumes ingestion. **Delete** permanently removes the
truck and all related readings, incidents, and alert history after confirmation.

Real devices can post JSON readings to the authenticated
`POST /api/v1/sensors/readings` endpoint with `truckId`, `temperature`, and an
ISO-8601 `timestamp`. The dashboard's optional simulator posts test readings
through this same analysis path; excursion mode sends temperatures above the
configured threshold and still observes the sustained-duration trigger.

Incident notifications continue to be recorded in alert history and logs.
Optional email delivery can be enabled with `ALERT_EMAIL_ENABLED=true` and
configured with `ALERT_EMAIL_TO`, `ALERT_EMAIL_FROM`, and
`AZURE_COMMUNICATION_SERVICES_ENDPOINT`. Delivery uses the API's Azure identity.
Optional webhook delivery uses `ALERT_WEBHOOK_URL`; its optional bearer token is
loaded from the `ALERT-WEBHOOK-BEARER-TOKEN` Key Vault secret. External delivery
is disabled when these settings are omitted.

## Build and test without Docker

```sh
mvn verify
cd frontend
npm ci
npm run build
```

The Java sources and tests are under
`backend/temperature-analysis-service/src`; Flyway SQL migrations live only in
`database/flyway`.
