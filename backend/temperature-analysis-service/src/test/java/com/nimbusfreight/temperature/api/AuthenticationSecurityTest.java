package com.nimbusfreight.temperature.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(
        controllers = {ApiController.class, AuthenticationController.class},
        properties = {
                "spring.security.operator.username=test-operator",
                "spring.security.operator.password=test-password-with-at-least-24-characters"
        })
@Import(SecurityConfiguration.class)
class AuthenticationSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void apiRequiresAnAuthenticatedSession() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void csrfBootstrapReturnsSessionBoundToken() throws Exception {
        var response = mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        String token = objectMapper.readTree(response.getResponse().getContentAsString()).get("token").asText();
        org.junit.jupiter.api.Assertions.assertFalse(token.isBlank());
    }

    @Test
    void loginCreatesAuthenticatedSession() throws Exception {
        AuthenticatedSession authenticated = loginWithCsrf();
        mockMvc.perform(get("/api/v1/auth/session").session(authenticated.session()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"test-operator\"}"));
    }

    @Test
    void loginRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .param("username", "test-operator")
                        .param("password", "test-password-with-at-least-24-characters"))
                .andExpect(status().isForbidden());
    }

    @Test
    void stateChangingRequestsRequireCsrfToken() throws Exception {
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
        AuthenticatedSession authenticated = loginWithCsrf();
        mockMvc.perform(put("/api/v1/config/thresholds")
                        .session(authenticated.session())
                        .header("X-CSRF-TOKEN", authenticated.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"thresholdCelsius\":8.0}"))
                .andExpect(status().isOk());
    }

    private AuthenticatedSession loginWithCsrf() throws Exception {
        MvcResult csrf = mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
        MockHttpSession session = (MockHttpSession) csrf.getRequest().getSession(false);

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .session(session)
                        .header("X-CSRF-TOKEN", token)
                        .param("username", "test-operator")
                        .param("password", "test-password-with-at-least-24-characters"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"test-operator\"}"))
                .andReturn();

        MockHttpSession sessionAfterLogin = (MockHttpSession) login.getRequest().getSession(false);
        MvcResult refreshedCsrf = mockMvc.perform(get("/api/v1/auth/csrf").session(sessionAfterLogin))
                .andExpect(status().isOk())
                .andReturn();
        String refreshedToken = objectMapper.readTree(refreshedCsrf.getResponse().getContentAsString())
                .get("token").asText();
        return new AuthenticatedSession(
                (MockHttpSession) refreshedCsrf.getRequest().getSession(false),
                refreshedToken);
    }

    private record AuthenticatedSession(MockHttpSession session, String token) {}
}
