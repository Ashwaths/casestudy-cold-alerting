package com.nimbusfreight.temperature.service;

import com.nimbusfreight.temperature.model.*;
import com.nimbusfreight.temperature.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class TemperatureAnalysisService {
    private final SensorReadingRepository readings;
    private final IncidentRepository incidents;
    private final AlertHistoryRepository alerts;
    private final ConfigurationRepository configs;
    private final NotificationService notifications;
    @Value("${nimbus.temperature.default-threshold-celsius:8.0}") private double defaultThreshold;
    @Value("${nimbus.temperature.warning-duration-minutes:15}") private long warningMinutes;
    @Value("${nimbus.temperature.recovery-duration-minutes:10}") private long recoveryMinutes;
    @Value("${nimbus.temperature.data-gap-minutes:10}") private long gapMinutes;

    public TemperatureAnalysisService(SensorReadingRepository readings, IncidentRepository incidents,
        AlertHistoryRepository alerts, ConfigurationRepository configs, NotificationService notifications) {
        this.readings=readings; this.incidents=incidents; this.alerts=alerts; this.configs=configs; this.notifications=notifications;
    }

    private double threshold() {
        return configs.findById("temperature.threshold.celsius").map(c -> Double.parseDouble(c.getValue())).orElse(defaultThreshold);
    }

    @Transactional
    public void reevaluate(String truckId) {
        List<SensorReading> all = readings.findByTruckTruckIdOrderByTimestampAsc(truckId);
        if (all.isEmpty()) return;
        // Evaluate event time, not arrival time. A large production dataset should use a stream processor/watermark.
        List<SensorReading> ordered = all.stream().sorted(Comparator.comparing(SensorReading::getTimestamp)).toList();
        double limit = threshold();
        Incident open = incidents.findFirstByTruckTruckIdAndStatusOrderByStartTimeDesc(truckId, "ACTIVE").orElse(null);

        Instant hotStart = null;
        Instant coolStart = null;
        double peak = Double.NEGATIVE_INFINITY;
        SensorReading previous = null;
        for (SensorReading r : ordered) {
            if (previous != null && Duration.between(previous.getTimestamp(), r.getTimestamp()).toMinutes() > gapMinutes) {
                // A gap breaks continuity; do not infer sustained temperature through missing telemetry.
                hotStart = null; coolStart = null;
            }
            if (r.getTemperature() > limit) {
                if (hotStart == null) hotStart = r.getTimestamp();
                coolStart = null;
                peak = Math.max(peak, r.getTemperature());
                if (open == null && Duration.between(hotStart, r.getTimestamp()).toMinutes() >= warningMinutes) {
                    open = new Incident(r.getTruck(), hotStart, peak, "ACTIVE");
                    incidents.save(open);
                    alerts.save(new AlertHistory(open, Instant.now(), "INCIDENT_OPENED"));
                    notifications.send("INCIDENT_OPENED", open);
                } else if (open != null) {
                    open.updatePeak(r.getTemperature());
                }
            } else {
                hotStart = null;
                peak = Double.NEGATIVE_INFINITY;
                if (open != null) {
                    if (coolStart == null) coolStart = r.getTimestamp();
                    if (Duration.between(coolStart, r.getTimestamp()).toMinutes() >= recoveryMinutes) {
                        open.resolve(r.getTimestamp());
                        incidents.save(open);
                        alerts.save(new AlertHistory(open, Instant.now(), "INCIDENT_RESOLVED"));
                        notifications.send("INCIDENT_RESOLVED", open);
                        open = null;
                        coolStart = null;
                    }
                }
            }
            previous = r;
        }
    }
}
