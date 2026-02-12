package com.hoatv.spring.cloud.app;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts JWT tokens from Keycloak to Spring Security authentication tokens.
 * Extracts roles from realm_access.roles claim and converts them to granted authorities.
 * 
 * Related to hvantran/project-management#187
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final Converter<Jwt, AbstractAuthenticationToken> delegate;

    public KeycloakJwtAuthenticationConverter() {
        this.delegate = new ReactiveJwtAuthenticationConverterAdapter(this::convertJwt);
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        return delegate.convert(jwt);
    }

    private AbstractAuthenticationToken convertJwt(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extracts roles from Keycloak JWT token's realm_access.roles claim
     * and converts them to Spring Security GrantedAuthority objects with ROLE_ prefix.
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
        
        return roles.stream()
                .map(role -> "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
