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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles logout by invalidating the session in both the gateway and Keycloak.
 * Redirects to Keycloak's end_session_endpoint for SSO logout.
 * 
 * Related to hvantran/project-management#187
 */
@Component
public class KeycloakLogoutHandler implements ServerLogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakLogoutHandler.class);

    private final String issuerUri;
    private final WebClient webClient;
    private final Set<String> allowedRedirectOrigins;
    private final String defaultPostLogoutRedirectUri;

    public KeycloakLogoutHandler(
            WebClient.Builder webClientBuilder,
            @Value("${KEYCLOAK_ISSUER_URI:http://localhost:6080/realms/pman-realm}") String issuerUri,
            @Value("${app.security.allowed-redirect-origins:${app.ui.allowed-origins:http://localhost:6084,http://localhost:6088,http://localhost:6090,http://localhost:3000}}")
            String allowedRedirectOrigins,
            @Value("${app.ui.url:http://localhost:6084}")
            String defaultPostLogoutRedirectUri) {
        this.webClient = webClientBuilder.build();
        this.issuerUri = issuerUri;
        this.defaultPostLogoutRedirectUri = defaultPostLogoutRedirectUri;
        this.allowedRedirectOrigins = Arrays.stream(allowedRedirectOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(auth -> auth.getPrincipal() instanceof OidcUser)
                .map(auth -> {
                    OidcUser oidcUser = (OidcUser) auth.getPrincipal();
                    String idToken = oidcUser.getIdToken() != null ? oidcUser.getIdToken().getTokenValue() : null;

                    UriComponentsBuilder builder = UriComponentsBuilder
                            .fromUriString(issuerUri)
                            .path("/protocol/openid-connect/logout")
                            .queryParam("post_logout_redirect_uri", getPostLogoutRedirectUri(exchange));

                    if (idToken != null) {
                        builder.queryParam("id_token_hint", idToken);
                    }

                    String logoutUrl = builder.build().toUriString();
                    logger.info("Logging out user {} from Keycloak", oidcUser.getPreferredUsername());
                    return logoutUrl;
                })
                .defaultIfEmpty(getPostLogoutRedirectUri(exchange))
                .flatMap(destinationUrl -> {
                    exchange.getExchange().getResponse().setStatusCode(
                            org.springframework.http.HttpStatus.FOUND
                    );
                    exchange.getExchange().getResponse().getHeaders()
                            .setLocation(URI.create(destinationUrl));
                    return exchange.getExchange().getResponse().setComplete();
                });
    }

    private String getPostLogoutRedirectUri(WebFilterExchange exchange) {
        String queryRedirectUri = exchange.getExchange().getRequest().getQueryParams().getFirst("redirect_uri");
        if (queryRedirectUri != null && !queryRedirectUri.isBlank()) {
            try {
                URI parsed = new URI(queryRedirectUri);
                String origin = extractOrigin(parsed);
                if (origin != null && allowedRedirectOrigins.contains(origin)) {
                    return queryRedirectUri;
                }
                logger.warn("Blocked redirect_uri with non-allowlisted origin in logout: {}", queryRedirectUri);
            } catch (URISyntaxException e) {
                logger.warn("Invalid redirect_uri format in logout: {}", queryRedirectUri);
            }
        }

        String referer = exchange.getExchange().getRequest().getHeaders().getFirst("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                URI parsed = new URI(referer);
                String origin = extractOrigin(parsed);
                if (origin != null && allowedRedirectOrigins.contains(origin)) {
                    return referer;
                }
            } catch (URISyntaxException e) {
                logger.warn("Invalid Referer format in logout: {}", referer);
            }
        }

        if (defaultPostLogoutRedirectUri != null && !defaultPostLogoutRedirectUri.isBlank()) {
            return defaultPostLogoutRedirectUri;
        }

        String scheme = exchange.getExchange().getRequest().getURI().getScheme();
        String host = exchange.getExchange().getRequest().getURI().getHost();
        int port = exchange.getExchange().getRequest().getURI().getPort();
        
        return String.format("%s://%s%s/", 
                scheme, 
                host, 
                (port != -1 && port != 80 && port != 443) ? ":" + port : "");
    }

    private String extractOrigin(URI uri) {
        if (uri.getScheme() == null || uri.getHost() == null) {
            return null;
        }
        StringBuilder origin = new StringBuilder();
        origin.append(uri.getScheme()).append("://").append(uri.getHost());
        if (uri.getPort() != -1) {
            origin.append(":").append(uri.getPort());
        }
        return origin.toString();
    }
}
