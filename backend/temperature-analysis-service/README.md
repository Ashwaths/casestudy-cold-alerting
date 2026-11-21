# Cold-chain API

This Spring Boot module is the single deployable backend for the case study.
Its `api`, `service`, `model`, and `repo` packages implement the ingestion,
temperature-analysis, incident, notification, and persistence boundaries in
one process, as described in [`../../docs/architecture.md`](../../docs/architecture.md).

Build and test it from the repository root with `mvn verify`. The root Maven
project aggregates this module. Database migrations are maintained in
`src/main/resources/db/migration`, the standard Flyway classpath location, and
packaged into the application by this module.
