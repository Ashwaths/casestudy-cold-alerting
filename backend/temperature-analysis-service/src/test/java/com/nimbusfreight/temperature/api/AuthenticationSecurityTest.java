package com.nimbusfreight.temperature.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.nimbusfreight.temperature.config.SecurityConfiguration;
import com.nimbusfreight.temperature.model.Configuration;
import com.nimbusfreight.temperature.repo.AlertHistoryRepository;
import com.nimbusfreight.temperature.repo.ConfigurationRepository;
import com.nimbusfreight.temperature.repo.IncidentRepository;
import com.nimbusfreight.temperature.repo.SensorReadingRepository;
import com.nimbusfreight.temperature.repo.TruckRepository;
import com.nimbusfreight.temperature.service.SensorIngestionService;
import com.nimbusfreight.temperature.service.TruckManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(
        controllers = {ApiController.class, AuthenticationController.class},
        properties = {
                "spring.profiles.active=test",
                "spring.security.oauth2.client.registration.azure.client-id=test-client",
                "spring.security.oauth2.client.registration.azure.client-secret=test-secret",
                "spring.security.oauth2.client.registration.azure.authorization-grant-type=authorization_code",
                "spring.security.oauth2.client.registration.azure.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}",
                "spring.security.oauth2.client.registration.azure.scope=openid,profile,email",
                "spring.security.oauth2.client.provider.azure.authorization-uri=https://login.microsoftonline.com/test-tenant/oauth2/v2.0/authorize",
                "spring.security.oauth2.client.provider.azure.token-uri=https://login.microsoftonline.com/test-tenant/oauth2/v2.0/token",
                "spring.security.oauth2.client.provider.azure.jwk-set-uri=https://login.microsoftonline.com/test-tenant/discovery/v2.0/keys",
                "spring.security.oauth2.client.provider.azure.user-info-uri=https://graph.microsoft.com/oidc/userinfo",
                "spring.security.oauth2.client.provider.azure.user-name-attribute=sub"
        })
@Import(SecurityConfiguration.class)
@SuppressWarnings("null")
class AuthenticationSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SensorIngestionService ingestion;

    @MockitoBean
    private TruckRepository trucks;

    @MockitoBean
    private IncidentRepository incidents;

    @MockitoBean
    private AlertHistoryRepository alerts;

    @MockitoBean
    private ConfigurationRepository configs;

    @MockitoBean
    private SensorReadingRepository readings;

    @MockitoBean
    private TruckManagementService truckManagement;

    @Test
    void apiRequiresAnAuthenticatedSession() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void csrfBootstrapReturnsSessionBoundToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "operator@example.com")
    void authenticatedSessionExposesTheIdentityName() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"operator@example.com\"}"));
    }

    @Test
    void oidcSessionUsesTheEntraPreferredUsernameClaim() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session")
                        .with(oidcLogin().idToken(token -> token.claim(
                                "preferred_username", "operator@example.com"))))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"operator@example.com\"}"));
    }

    @Test
    void protectedMutationRejectsAnonymousRequestEvenWithCsrf() throws Exception {
        mockMvc.perform(put("/api/v1/config/thresholds")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"thresholdCelsius\":8.0}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedStateChangingRequestsAcceptCsrfToken() throws Exception {
        when(configs.findById(anyString())).thenReturn(Optional.empty());
        when(configs.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mockMvc.perform(put("/api/v1/config/thresholds")
                        .with(user("operator@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"thresholdCelsius\":8.0}"))
                .andExpect(status().isOk());
    }
}
