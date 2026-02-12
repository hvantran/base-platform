package com.hoatv.spring.cloud.app.controller;

import com.hoatv.spring.cloud.app.dto.UserInfoResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for user information
 * Returns authenticated user details from JWT token
 */
@RestController
@RequestMapping("/api/userinfo")
public class UserInfoController {

    /**
     * Get current authenticated user information
     * Extracts user details from JWT token claims
     * 
     * @param principal the authenticated principal
     * @return UserInfoResponse with user details
     */
    @GetMapping
    public Mono<UserInfoResponse> getUserInfo(Mono<Principal> principal) {
        return principal
                .map(p -> {
                    if (p instanceof Authentication) {
                        Authentication auth = (Authentication) p;
                        
                        // Extract username
                        String username = auth.getName();
                        
                        // Extract roles (already prefixed with ROLE_ by KeycloakJwtAuthenticationConverter)
                        List<String> roles = auth.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList());
                        
                        // Extract additional claims from JWT
                        String email = null;
                        String name = null;
                        
                        if (auth instanceof JwtAuthenticationToken) {
                            Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
                            email = jwt.getClaimAsString("email");
                            name = jwt.getClaimAsString("name");
                            
                            // Fallback to preferred_username if name is not available
                            if (name == null || name.isEmpty()) {
                                name = jwt.getClaimAsString("preferred_username");
                            }
                        }
                        
                        return new UserInfoResponse(username, email, name, roles, true);
                    }
                    
                    // User not authenticated (shouldn't happen if endpoint is secured)
                    return new UserInfoResponse(null, null, null, Collections.emptyList(), false);
                })
                .defaultIfEmpty(new UserInfoResponse(null, null, null, Collections.emptyList(), false));
    }
}
