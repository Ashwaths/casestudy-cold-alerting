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

1. Copy `.env.example` to `.env`. Set unique values for `MYSQL_PASSWORD`,
   `MYSQL_ROOT_PASSWORD`, `APP_USERNAME`, and `APP_PASSWORD`. `APP_PASSWORD`
   must be at least 24 characters. Generate passwords with
   `openssl rand -base64 32`. Set `SERVER_NAME` to the hostname in your TLS
   certificate. Do not commit `.env`.
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
   `APP_USERNAME` and `APP_PASSWORD`; the dashboard uses a CSRF-protected
   server session. The API and database are not published to the host.
5. Check service status and logs:

   ```sh
   docker compose ps
   docker compose logs -f api
   ```

Flyway initializes the database and seeds the demo truck on first start. MySQL
data is stored in the `mysql_data` volume. Stop the stack with
`docker compose down`; add `-v` only when intentionally deleting database data.

Authentication uses one environment-configured operator account with BCrypt
password hashing, a secure session cookie, and CSRF protection. This is a
single-operator case-study setup, not an enterprise identity provider or
per-user authorization system. Never publish the API or database ports
directly.

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
