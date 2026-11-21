package com.nimbusfreight.temperature.service;

import com.nimbusfreight.temperature.api.TruckRequest;
import com.nimbusfreight.temperature.api.TruckStatusResponse;
import com.nimbusfreight.temperature.model.*;
import com.nimbusfreight.temperature.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@SuppressWarnings("null")
public class TruckManagementService {
    private final TruckRepository trucks;
    private final SensorReadingRepository readings;
    private final IncidentRepository incidents;
    private final AlertHistoryRepository alerts;

    public TruckManagementService(TruckRepository trucks, SensorReadingRepository readings,
            IncidentRepository incidents, AlertHistoryRepository alerts) {
        this.trucks = trucks;
        this.readings = readings;
        this.incidents = incidents;
        this.alerts = alerts;
    }

    @Transactional(readOnly = true)
    public List<TruckStatusResponse> list() {
        return trucks.findAllByOrderByTruckIdAsc().stream().map(this::status).toList();
    }

    @Transactional
    public TruckStatusResponse create(TruckRequest request) {
        if (trucks.existsById(request.truckId())) {
            throw new IllegalArgumentException("Truck ID already exists");
        }
        if (trucks.existsByRegistrationNumber(request.registrationNumber())) {
            throw new IllegalArgumentException("Registration number already exists");
        }
        return status(trucks.save(new Truck(request.truckId(), request.registrationNumber(), request.customerName())));
    }

    @Transactional
    public TruckStatusResponse update(String truckId, TruckRequest request) {
        if (!truckId.equals(request.truckId())) {
            throw new IllegalArgumentException("Truck ID cannot be changed");
        }
        Truck truck = findTruck(truckId);
        if (trucks.existsByRegistrationNumberAndTruckIdNot(request.registrationNumber(), truckId)) {
            throw new IllegalArgumentException("Registration number already exists");
        }
        truck.updateDetails(request.registrationNumber(), request.customerName());
        return status(truck);
    }

    @Transactional
    public TruckStatusResponse archive(String truckId) {
        Truck truck = findTruck(truckId);
        truck.setStatus("INACTIVE");
        return status(truck);
    }

    @Transactional
    public TruckStatusResponse activate(String truckId) {
        Truck truck = findTruck(truckId);
        truck.setStatus("ACTIVE");
        return status(truck);
    }

    @Transactional
    public void permanentlyDelete(String truckId) {
        Truck truck = findTruck(truckId);
        List<Incident> truckIncidents = incidents.findAllByTruckTruckId(truckId);
        alerts.deleteAllByIncidentIn(truckIncidents);
        incidents.deleteAll(truckIncidents);
        readings.deleteAllByTruckTruckId(truckId);
        trucks.delete(truck);
    }

    @Transactional(readOnly = true)
    public TruckStatusResponse status(String truckId) {
        return status(findTruck(truckId));
    }

    private TruckStatusResponse status(Truck truck) {
        SensorReading latest = readings.findFirstByTruckTruckIdOrderByTimestampDesc(truck.getTruckId()).orElse(null);
        Incident open = incidents.findFirstByTruckTruckIdAndStatusOrderByStartTimeDesc(truck.getTruckId(), "ACTIVE")
                .orElse(null);
        String telemetry = latest == null ? "NO_DATA" :
                Duration.between(latest.getTimestamp(), Instant.now()).toMinutes() > 10 ? "STALE" : "CURRENT";
        return new TruckStatusResponse(
                truck.getTruckId(), truck.getRegistrationNumber(), truck.getCustomerName(), truck.getStatus(),
                latest == null ? null : latest.getTemperature(), latest == null ? null : latest.getTimestamp(),
                telemetry, open == null ? "NORMAL" : open.getStatus());
    }

    private Truck findTruck(String truckId) {
        return trucks.findById(truckId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown truckId: " + truckId));
    }
}
