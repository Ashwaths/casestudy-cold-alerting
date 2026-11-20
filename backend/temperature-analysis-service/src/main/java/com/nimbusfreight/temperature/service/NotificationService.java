package com.nimbusfreight.temperature.service;

import com.nimbusfreight.temperature.model.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    public void send(String type, Incident incident) {
        // Replace with email/Teams adapters. Use an outbox + retries in production.
        log.info("Notification type={} incident={} truck={} status={}",
                type, incident.getIncidentId(), incident.getTruck().getTruckId(), incident.getStatus());
    }
}
