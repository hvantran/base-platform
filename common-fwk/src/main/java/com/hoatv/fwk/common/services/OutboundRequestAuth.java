package com.hoatv.fwk.common.services;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Holds the caller's Bearer token for the duration of a job script.
 * Used so outbound HTTP to internal services can run as the same user.
 * Never put this token into logged script execution context.
 */
public final class OutboundRequestAuth {

    private static final ThreadLocal<String> BEARER_TOKEN = new ThreadLocal<>();

    private static final Set<String> INTERNAL_HOSTS = Set.of(
            "localhost",
            "127.0.0.1",
            "action-manager-backend",
            "action-manager",
            "spring-kafka-notifier",
            "keycloak"
    );

    public static final String AUTHORIZATION = "Authorization";

    private OutboundRequestAuth() {
    }

    public static void setBearerToken(String token) {
        if (StringUtils.isBlank(token)) {
            BEARER_TOKEN.remove();
            return;
        }
        BEARER_TOKEN.set(token);
    }

    public static Optional<String> getBearerToken() {
        return Optional.ofNullable(BEARER_TOKEN.get()).filter(StringUtils::isNotBlank);
    }

    public static void clear() {
        BEARER_TOKEN.remove();
    }

    public static boolean isInternalUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            String normalized = host.toLowerCase(Locale.ROOT);
            if (INTERNAL_HOSTS.contains(normalized)) {
                return true;
            }
            return normalized.endsWith(".local");
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * Adds Bearer auth when calling an internal host and the caller did not set Authorization.
     * Public hosts (Lazada, Hasaki, etc.) are left unchanged.
     */
    public static void mergeAuthorizationHeader(String url, Map<String, String> headers) {
        if (headers == null || !isInternalUrl(url)) {
            return;
        }
        boolean alreadySet = headers.keySet().stream()
                .anyMatch(key -> AUTHORIZATION.equalsIgnoreCase(key));
        if (alreadySet) {
            return;
        }
        getBearerToken().ifPresent(token -> headers.put(AUTHORIZATION, "Bearer " + token));
    }
}
