package com.nimbusfreight.temperature.api;

import com.nimbusfreight.temperature.model.*;
import com.nimbusfreight.temperature.repo.*;
import com.nimbusfreight.temperature.service.SensorIngestionService;
import com.nimbusfreight.temperature.service.TruckManagementService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class ApiController {
    private final SensorIngestionService ingestion;
    private final IncidentRepository incidents;
    private final AlertHistoryRepository alerts;
    private final ConfigurationRepository configs;
    private final TruckManagementService truckManagement;

    public ApiController(SensorIngestionService ingestion, IncidentRepository incidents,
                         AlertHistoryRepository alerts, ConfigurationRepository configs,
                         TruckManagementService truckManagement) {
        this.ingestion=ingestion; this.incidents=incidents; this.alerts=alerts; this.configs=configs;
        this.truckManagement=truckManagement;
    }

    @GetMapping("/trucks")
    public List<TruckStatusResponse> trucks() { return truckManagement.list(); }

    @PostMapping("/trucks")
    public ResponseEntity<TruckStatusResponse> createTruck(@Valid @RequestBody TruckRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(truckManagement.create(request));
    }

    @PutMapping("/trucks/{truckId}")
    public TruckStatusResponse updateTruck(@PathVariable String truckId, @Valid @RequestBody TruckRequest request) {
        return truckManagement.update(truckId, request);
    }

    @PostMapping("/trucks/{truckId}/archive")
    public TruckStatusResponse archiveTruck(@PathVariable String truckId) {
        return truckManagement.archive(truckId);
    }

    @PostMapping("/trucks/{truckId}/activate")
    public TruckStatusResponse activateTruck(@PathVariable String truckId) {
        return truckManagement.activate(truckId);
    }

    @DeleteMapping("/trucks/{truckId}")
    public ResponseEntity<Void> permanentlyDeleteTruck(@PathVariable String truckId) {
        truckManagement.permanentlyDelete(truckId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sensors/readings")
    public ResponseEntity<Map<String,Object>> ingest(@Valid @RequestBody ReadingRequest request) {
        return ResponseEntity.accepted().body(ingestion.ingest(request));
    }

    @GetMapping("/trucks/{truckId}/status")
    public TruckStatusResponse truckStatus(@PathVariable String truckId) { return truckManagement.status(truckId); }

    @GetMapping("/incidents/active")
    public List<Incident> activeIncidents() { return incidents.findByStatusOrderByStartTimeDesc("ACTIVE"); }

    @GetMapping("/incidents/{incidentId}")
    public Incident incident(@PathVariable Long incidentId) {
        return incidents.findByIncidentId(incidentId).orElseThrow(() -> new IllegalArgumentException("Unknown incidentId: " + incidentId));
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
