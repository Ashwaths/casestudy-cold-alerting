package com.nimbusfreight.temperature.config;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class NotificationConfiguration {
    @Bean
    @ConditionalOnProperty(name = "nimbus.alerts.email.enabled", havingValue = "true")
    EmailClient emailClient(@Value("${nimbus.alerts.email.communication-services.endpoint}") String endpoint) {
        if (!"https".equalsIgnoreCase(URI.create(endpoint).getScheme())) {
            throw new IllegalStateException("Azure Communication Services endpoint must use HTTPS");
        }
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        return new EmailClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();
    }
}
