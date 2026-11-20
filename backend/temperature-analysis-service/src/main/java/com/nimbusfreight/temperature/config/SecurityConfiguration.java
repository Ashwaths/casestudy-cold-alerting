package com.nimbusfreight.temperature.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Configuration
public class SecurityConfiguration {
    private static final int MINIMUM_PASSWORD_LENGTH = 24;

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService operatorUserDetailsService(
            @Value("${spring.security.operator.username}") String username,
            @Value("${spring.security.operator.password}") String password,
            PasswordEncoder passwordEncoder) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalStateException("APP_USERNAME must not be blank");
        }
        if (password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalStateException("APP_PASSWORD must contain at least 24 characters");
        }
        return new InMemoryUserDetailsManager(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles("OPERATOR")
                .build());
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        return repository;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CsrfTokenRepository csrfTokenRepository,
            ObjectMapper objectMapper) throws Exception {
        AuthenticationSuccessHandler successHandler = (request, response, authentication) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), Map.of("username", authentication.getName()));
        };
        AuthenticationFailureHandler failureHandler = (request, response, exception) ->
                response.sendError(HttpStatus.UNAUTHORIZED.value());
        AuthenticationEntryPoint entryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);

        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/api/v1/auth/csrf", "/api/v1/auth/login").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint))
                .formLogin(form -> form
                        .loginProcessingUrl("/api/v1/auth/login")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .build();
    }
}
