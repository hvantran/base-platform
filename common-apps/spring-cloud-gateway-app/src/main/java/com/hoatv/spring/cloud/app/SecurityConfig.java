package com.hoatv.spring.cloud.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

/**
 * Security configuration for Spring Cloud Gateway with Keycloak integration.
 * Implements the backoffice pattern with session-based OAuth2 authentication.
 * 
 * Related to hvantran/project-management#187
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final KeycloakLogoutHandler keycloakLogoutHandler;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(
            KeycloakLogoutHandler keycloakLogoutHandler,
            @Lazy OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.keycloakLogoutHandler = keycloakLogoutHandler;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }
    
    /**
     * Security context repository that stores authentication in WebSession (cookies)
     */
    @Bean
    public WebSessionServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            WebSessionServerSecurityContextRepository securityContextRepository) {
        http
            // CORS configuration - must be before other filters
            .cors(cors -> cors.disable()) // Using CorsWebFilter instead
            
            // Explicitly configure security context repository to use WebSession
            .securityContextRepository(securityContextRepository)
            
            // OAuth2 Login for user authentication (session-based)
            .oauth2Login(oauth2 -> oauth2
                .authenticationSuccessHandler(oAuth2LoginSuccessHandler)
            )
            
            // Authorization rules
            .authorizeExchange(exchanges -> exchanges
                // Allow CORS preflight requests (OPTIONS)
                .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                
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
