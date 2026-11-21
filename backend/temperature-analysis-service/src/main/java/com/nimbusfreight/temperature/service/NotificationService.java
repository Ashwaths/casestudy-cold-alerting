package com.nimbusfreight.temperature.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.nimbusfreight.temperature.model.Incident;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;

@Service
@SuppressWarnings("null")
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ObjectProvider<EmailClient> emailClientProvider;
    private final RestClient restClient;
    private final boolean emailEnabled;
    private final String emailTo;
    private final String emailFrom;
    private final String webhookUrl;
    private final String webhookBearerToken;

    public NotificationService(ObjectProvider<EmailClient> emailClientProvider, RestClient.Builder restClientBuilder,
            @Value("${nimbus.alerts.email.enabled:false}") boolean emailEnabled,
            @Value("${nimbus.alerts.email.to:}") String emailTo,
            @Value("${nimbus.alerts.email.from:}") String emailFrom,
            @Value("${nimbus.alerts.webhook.url:}") String webhookUrl,
            @Value("${nimbus.alerts.webhook.bearer-token:}") String webhookBearerToken) {
        this.emailClientProvider = emailClientProvider;
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
            if (!StringUtils.hasText(emailFrom)) {
                throw new IllegalStateException("ALERT_EMAIL_FROM is required when email alerts are enabled");
            }
            EmailClient emailClient = emailClientProvider.getIfAvailable();
            if (emailClient == null) {
                throw new IllegalStateException("Azure Communication Services email is not configured");
            }
            EmailMessage email = new EmailMessage()
                    .setSenderAddress(emailFrom)
                    .setToRecipients(emailTo)
                    .setSubject(subject)
                    .setBodyPlainText(message);
            emailClient.beginSend(email).getFinalResult();
        }

        if (StringUtils.hasText(webhookUrl)) {
            if (!"https".equalsIgnoreCase(URI.create(webhookUrl).getScheme())) {
                throw new IllegalArgumentException("ALERT_WEBHOOK_URL must use HTTPS");
            }
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
