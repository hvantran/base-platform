package com.hoatv.spring.cloud.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for Spring Cloud Gateway with Keycloak integration.
 * Implements the backoffice pattern with:
 * - OAuth2 Login for user authentication via Keycloak
 * - OAuth2 Resource Server for JWT token validation
 * - Role-based access control
 * - SSO logout integration
 * 
 * Related to hvantran/project-management#187
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final KeycloakJwtAuthenticationConverter jwtAuthenticationConverter;
    private final KeycloakLogoutHandler keycloakLogoutHandler;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(
            KeycloakJwtAuthenticationConverter jwtAuthenticationConverter,
            KeycloakLogoutHandler keycloakLogoutHandler,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.keycloakLogoutHandler = keycloakLogoutHandler;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            // OAuth2 Login for user authentication
            .oauth2Login(oauth2 -> oauth2
                .authenticationSuccessHandler(oAuth2LoginSuccessHandler)
            )
            
            // OAuth2 Resource Server for JWT validation
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            )
            
            // Authorization rules
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Actuator endpoints require authentication
                .pathMatchers("/actuator/**").authenticated()
                
                // API endpoints require authentication
                // Fine-grained authorization is handled by downstream services
                .pathMatchers("/api/**").authenticated()
                
                // All other requests require authentication
                .anyExchange().authenticated()
            )
            
            // Logout configuration with Keycloak SSO logout
            .logout(logout -> logout
                .logoutSuccessHandler(keycloakLogoutHandler)
            )
            
            // Disable form login and HTTP basic (using OAuth2 only)
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            
            // CSRF can be disabled for stateless API gateway
            .csrf(csrf -> csrf.disable());
            
        return http.build();
    }
}
