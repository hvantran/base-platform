package com.hoatv.spring.cloud.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Captures redirect_uri from login requests and stores it in session.
 * Only allowlisted origins are accepted to prevent open-redirect attacks.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoginRedirectCaptureWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoginRedirectCaptureWebFilter.class);
    private static final String LOGIN_PATH = "/oauth2/authorization/keycloak";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String REDIRECT_URI_SESSION_ATTR = "REDIRECT_URI";

    private final Set<String> allowedRedirectOrigins;

    public LoginRedirectCaptureWebFilter(
            @Value("${app.security.allowed-redirect-origins:http://localhost:6084,http://localhost:6088}")
            String allowedRedirectOrigins) {
        this.allowedRedirectOrigins = Arrays.stream(allowedRedirectOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (!LOGIN_PATH.equals(request.getPath().value())) {
            return chain.filter(exchange);
        }

        String redirectUri = request.getQueryParams().getFirst(REDIRECT_URI_PARAM);
        if (redirectUri == null || redirectUri.isBlank()) {
            return chain.filter(exchange);
        }

        URI parsedRedirectUri;
        try {
            parsedRedirectUri = new URI(redirectUri);
        } catch (URISyntaxException e) {
            logger.warn("Invalid redirect_uri format: {}", redirectUri);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        String origin = extractOrigin(parsedRedirectUri);
        if (origin == null || !allowedRedirectOrigins.contains(origin)) {
            logger.warn("Blocked redirect_uri with non-allowlisted origin: {}", redirectUri);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        return exchange.getSession()
                .doOnNext(session -> session.getAttributes().put(REDIRECT_URI_SESSION_ATTR, redirectUri))
                .then(chain.filter(exchange));
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
