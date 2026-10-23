package com.nimbusfreight.temperature.api;

import com.nimbusfreight.temperature.model.*;
import com.nimbusfreight.temperature.repo.*;
import com.nimbusfreight.temperature.service.SensorIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class ApiController {
    private final SensorIngestionService ingestion;
    private final TruckRepository trucks;
    private final IncidentRepository incidents;
    private final AlertHistoryRepository alerts;
    private final ConfigurationRepository configs;
    private final SensorReadingRepository readings;

    public ApiController(SensorIngestionService ingestion, TruckRepository trucks, IncidentRepository incidents,
                         AlertHistoryRepository alerts, ConfigurationRepository configs, SensorReadingRepository readings) {
        this.ingestion=ingestion; this.trucks=trucks; this.incidents=incidents; this.alerts=alerts; this.configs=configs; this.readings=readings;
    }

    @PostMapping("/sensors/readings")
    public ResponseEntity<Map<String,Object>> ingest(@Valid @RequestBody ReadingRequest request) {
        return ResponseEntity.accepted().body(ingestion.ingest(request));
    }

    @GetMapping("/trucks/{truckId}/status")
    public Map<String,Object> truckStatus(@PathVariable String truckId) {
        Truck truck = trucks.findById(truckId).orElseThrow(() -> new IllegalArgumentException("Unknown truckId: " + truckId));
        List<SensorReading> rs = readings.findByTruckTruckIdOrderByTimestampAsc(truckId);
        SensorReading latest = rs.stream().max(Comparator.comparing(SensorReading::getTimestamp)).orElse(null);
        Optional<Incident> open = incidents.findFirstByTruckTruckIdAndStatusOrderByStartTimeDesc(truckId, "ACTIVE");
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("truckId", truck.getTruckId()); result.put("registrationNumber", truck.getRegistrationNumber());
        result.put("customerName", truck.getCustomerName()); result.put("truckStatus", truck.getStatus());
        result.put("latestTemperature", latest == null ? null : latest.getTemperature());
        result.put("latestReadingAt", latest == null ? null : latest.getTimestamp());
        result.put("telemetryState", latest == null ? "NO_DATA" :
            Duration.between(latest.getTimestamp(), Instant.now()).toMinutes() > 10 ? "STALE" : "CURRENT");
        result.put("incidentStatus", open.map(Incident::getStatus).orElse("NORMAL"));
        return result;
    }

    @GetMapping("/incidents/active")
    public List<Incident> activeIncidents() { return incidents.findByStatusOrderByStartTimeDesc("ACTIVE"); }

    @GetMapping("/incidents/{incidentId}")
    public Incident incident(@PathVariable Long incidentId) {
        return incidents.findById(incidentId).orElseThrow(() -> new IllegalArgumentException("Unknown incidentId: " + incidentId));
    }

    @GetMapping("/alerts/history")
    public List<AlertHistory> alertHistory() { return alerts.findAllByOrderByAlertTimeDesc(); }

    @PutMapping("/config/thresholds")
    public Map<String,Object> updateThreshold(@Valid @RequestBody ThresholdRequest request) {
        Configuration c = configs.findById("temperature.threshold.celsius")
            .orElseGet(() -> new Configuration("temperature.threshold.celsius", request.thresholdCelsius().toString()));
        c.setValue(request.thresholdCelsius().toString()); configs.save(c);
        return Map.of("thresholdCelsius", request.thresholdCelsius(), "updatedAt", Instant.now());
    }
}
