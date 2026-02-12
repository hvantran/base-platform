package com.hoatv.spring.cloud.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Handles logout by invalidating the session in both the gateway and Keycloak.
 * Redirects to Keycloak's end_session_endpoint for SSO logout.
 * 
 * Related to hvantran/project-management#187
 */
@Component
public class KeycloakLogoutHandler implements ServerLogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakLogoutHandler.class);

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    private final WebClient webClient;

    public KeycloakLogoutHandler(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(auth -> auth.getPrincipal() instanceof OidcUser)
                .flatMap(auth -> {
                    OidcUser oidcUser = (OidcUser) auth.getPrincipal();
                    String idToken = oidcUser.getIdToken().getTokenValue();
                    
                    // Construct Keycloak logout URL
                    String logoutUrl = UriComponentsBuilder
                            .fromUriString(issuerUri)
                            .path("/protocol/openid-connect/logout")
                            .queryParam("id_token_hint", idToken)
                            .queryParam("post_logout_redirect_uri", getPostLogoutRedirectUri(exchange))
                            .build()
                            .toUriString();

                    logger.info("Logging out user {} from Keycloak", oidcUser.getPreferredUsername());

                    // Redirect to Keycloak logout endpoint
                    return exchange.getExchange().getResponse()
                            .writeWith(Mono.empty())
                            .then(Mono.fromRunnable(() -> {
                                exchange.getExchange().getResponse().setStatusCode(
                                        org.springframework.http.HttpStatus.FOUND
                                );
                                exchange.getExchange().getResponse().getHeaders()
                                        .setLocation(URI.create(logoutUrl));
                            }));
                })
                .switchIfEmpty(
                        // If not OIDC user, just redirect to home
                        Mono.fromRunnable(() -> {
                            exchange.getExchange().getResponse().setStatusCode(
                                    org.springframework.http.HttpStatus.FOUND
                            );
                            exchange.getExchange().getResponse().getHeaders()
                                    .setLocation(URI.create("/"));
                        })
                );
    }

    private String getPostLogoutRedirectUri(WebFilterExchange exchange) {
        String baseUrl = exchange.getExchange().getRequest().getURI().toString();
        String scheme = exchange.getExchange().getRequest().getURI().getScheme();
        String host = exchange.getExchange().getRequest().getURI().getHost();
        int port = exchange.getExchange().getRequest().getURI().getPort();
        
        return String.format("%s://%s%s/", 
                scheme, 
                host, 
                (port != -1 && port != 80 && port != 443) ? ":" + port : "");
    }
}
