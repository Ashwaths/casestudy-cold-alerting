package com.nimbusfreight.temperature.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.SyncPoller;
import com.nimbusfreight.temperature.model.Incident;
import com.nimbusfreight.temperature.model.Truck;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.client.RestClient;
@SuppressWarnings("null")
class NotificationServiceTest {
    @Test
    void sendsExistingAlertContentThroughAzureCommunicationServices() {
        ObjectProvider<EmailClient> provider = mock();
        EmailClient emailClient = mock(EmailClient.class);
        SyncPoller<EmailSendResult, EmailSendResult> poller = mock();
        when(provider.getIfAvailable()).thenReturn(emailClient);
        when(emailClient.beginSend(any(EmailMessage.class))).thenReturn(poller);
        NotificationService service = new NotificationService(
                provider, RestClient.builder(), true, "ops@example.com",
                "alerts@example.com", "", "");
        Incident incident = new Incident(
                new Truck("TRUCK-1", "REG-1", "Customer"),
                Instant.parse("2026-01-01T00:00:00Z"), 9.5, "ACTIVE");

        service.send("INCIDENT_OPENED", incident);

        verify(emailClient).beginSend(any(EmailMessage.class));
        verify(poller).getFinalResult();
    }

    @Test
    void rejectsWebhookEndpointsThatDoNotUseHttps() {
        NotificationService service = new NotificationService(
                mock(), RestClient.builder(), false,
                "", "", "http://example.test/alerts", "");
        Incident incident = new Incident(
                new Truck("TRUCK-1", "REG-1", "Customer"),
                Instant.parse("2026-01-01T00:00:00Z"), 9.5, "ACTIVE");

        assertThrows(IllegalArgumentException.class, () -> service.send("INCIDENT_OPENED", incident));
    }
}
