# Architecture and key decisions

## Logical components
- Angular dashboard: operations view for truck telemetry and active incidents.
- API layer: REST contracts; in a production deployment put an authenticated gateway in front.
- Sensor ingestion: validate, timestamp-check, deduplicate, persist readings.
- Temperature analysis: evaluate by truck and event time, apply warning/incident/recovery windows.
- Alert management: create/update/resolve incidents and persist alert history.
- Notification adapter: email/Teams integration; production delivery should use an outbox, retries, and idempotency.
- MySQL: durable operational state and history.

## Business semantics
- Threshold is configurable, default 8°C.
- A hot period is eligible for an incident after 15 minutes of sustained readings above threshold.
- An active incident is resolved after 10 minutes of readings at or below threshold.
- Readings separated by a gap larger than the configured gap break the continuity window; missing data must be surfaced as stale/gap telemetry rather than treated as safe.
- One `INCIDENT_OPENED` and one `INCIDENT_RESOLVED` alert record are written for the lifecycle.

## Production hardening backlog
1. Add a sensor-generated event ID and unique constraint for robust idempotency.
2. Define event-time watermark / allowed lateness policy and replay behavior for late events.
3. Move analysis to Kafka Streams or another keyed stream processor for scale; partition by truck ID.
4. Add an outbox table and worker for reliable email/Teams delivery.
5. Add authentication/authorization, API rate limits, TLS, secret manager, audit trail.
6. Replace schema auto-update with Flyway/Liquibase migrations.
7. Add paging/retention policies and indexes for high-volume sensor data.
8. Add contract, integration, concurrency, and event-time boundary tests.
9. Add data-gap alert policy and operational dashboards/metrics.
10. Decide how threshold changes apply to already-open incidents and historical reprocessing.

## Important implementation caveat
The provided service is a runnable starter, not a complete distributed microservices deployment. It keeps domain boundaries in one Spring Boot process to make local setup straightforward; each module can be extracted behind internal APIs/events later. The current event-time evaluation scans stored readings and is suitable only for a small demo dataset, not high-volume production traffic.
