package com.nimbusfreight.temperature.service;

import com.nimbusfreight.temperature.model.Incident;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mailSender;
    private final RestClient restClient;
    private final boolean emailEnabled;
    private final String emailTo;
    private final String emailFrom;
    private final String webhookUrl;
    private final String webhookBearerToken;

    public NotificationService(JavaMailSender mailSender, RestClient.Builder restClientBuilder,
            @Value("${nimbus.alerts.email.enabled:false}") boolean emailEnabled,
            @Value("${nimbus.alerts.email.to:}") String emailTo,
            @Value("${nimbus.alerts.email.from:}") String emailFrom,
            @Value("${nimbus.alerts.webhook.url:}") String webhookUrl,
            @Value("${nimbus.alerts.webhook.bearer-token:}") String webhookBearerToken) {
        this.mailSender = mailSender;
        this.restClient = restClientBuilder.build();
        this.emailEnabled = emailEnabled;
        this.emailTo = emailTo;
        this.emailFrom = emailFrom;
        this.webhookUrl = webhookUrl;
        this.webhookBearerToken = webhookBearerToken;
    }

    public void send(String type, Incident incident) {
        log.info("Notification type={} incident={} truck={} status={}",
                type, incident.getIncidentId(), incident.getTruck().getTruckId(), incident.getStatus());

        String subject = "Cold-chain alert: " + type + " for " + incident.getTruck().getTruckId();
        String message = "Alert: " + type + "\nTruck: " + incident.getTruck().getTruckId()
                + "\nRegistration: " + incident.getTruck().getRegistrationNumber()
                + "\nIncident: " + incident.getIncidentId()
                + "\nStarted: " + incident.getStartTime()
                + "\nPeak temperature: " + incident.getPeakTemperature() + " C"
                + "\nStatus: " + incident.getStatus();

        if (emailEnabled) {
            if (!StringUtils.hasText(emailTo)) {
                throw new IllegalStateException("ALERT_EMAIL_TO is required when email alerts are enabled");
            }
            SimpleMailMessage email = new SimpleMailMessage();
            if (StringUtils.hasText(emailFrom)) email.setFrom(emailFrom);
            email.setTo(emailTo);
            email.setSubject(subject);
            email.setText(message);
            mailSender.send(email);
        }

        if (StringUtils.hasText(webhookUrl)) {
            var request = restClient.post().uri(webhookUrl).body(new WebhookAlert(
                    type, incident.getIncidentId(), incident.getTruck().getTruckId(),
                    incident.getTruck().getRegistrationNumber(), incident.getStartTime(),
                    incident.getPeakTemperature(), incident.getStatus()));
            if (StringUtils.hasText(webhookBearerToken)) {
                request.header("Authorization", "Bearer " + webhookBearerToken);
            }
            request.retrieve().toBodilessEntity();
        }
    }

    private record WebhookAlert(String type, Long incidentId, String truckId, String registrationNumber,
                                java.time.Instant startedAt, double peakTemperature, String status) {}
}
