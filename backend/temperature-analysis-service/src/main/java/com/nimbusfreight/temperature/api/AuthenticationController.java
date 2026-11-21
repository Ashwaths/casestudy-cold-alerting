package com.nimbusfreight.temperature.api;

import java.util.Map;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    @GetMapping("/session")
    public Map<String, String> session(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String username = oidcUser.getClaimAsString("preferred_username");
            if (username == null || username.isBlank()) {
                username = oidcUser.getClaimAsString("email");
            }
            if (username != null && !username.isBlank()) {
                return Map.of("username", username);
            }
        }
        return Map.of("username", authentication.getName());
    }
}
