package com.nimbusfreight.temperature.service;

import com.nimbusfreight.temperature.api.ReadingRequest;
import com.nimbusfreight.temperature.model.*;
import com.nimbusfreight.temperature.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class SensorIngestionService {
    private final TruckRepository trucks;
    private final SensorReadingRepository readings;
    private final TemperatureAnalysisService analysis;
    public SensorIngestionService(TruckRepository trucks, SensorReadingRepository readings, TemperatureAnalysisService analysis) {
        this.trucks=trucks; this.readings=readings; this.analysis=analysis;
    }

    @Transactional
    public Map<String,Object> ingest(ReadingRequest request) {
        Instant now = Instant.now();
        if (request.timestamp().isAfter(now.plus(Duration.ofMinutes(5))))
            throw new IllegalArgumentException("Sensor timestamp is too far in the future");
        Truck truck = trucks.findById(request.truckId())
            .orElseThrow(() -> new IllegalArgumentException("Unknown truckId: " + request.truckId()));
        // The payload lacks a sensor event ID. This deterministic key is a starter dedupe strategy.
        String key = request.truckId() + "|" + request.timestamp() + "|" + request.temperature();
        if (readings.existsByEventKey(key)) return Map.of("accepted", true, "duplicate", true, "truckId", request.truckId());
        readings.save(new SensorReading(truck, request.timestamp(), request.temperature(), now, key));
        analysis.reevaluate(request.truckId());
        return Map.of("accepted", true, "duplicate", false, "truckId", request.truckId());
    }
}
