package com.hoatv.spring.cloud.app.controller;

import com.hoatv.spring.cloud.app.dto.UserInfoResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for user information.
 * Returns authenticated user details from OAuth2/OIDC session.
 */
@RestController
@RequestMapping("/api/userinfo")
public class UserInfoController {

    /**
     * Get current authenticated user information.
     * Extracts user details from OAuth2/OIDC authentication.
     * 
     * @param principal the authenticated principal
     * @return UserInfoResponse with user details
     */
    @GetMapping
    public Mono<UserInfoResponse> getUserInfo(Mono<Principal> principal) {
        return principal
                .map(p -> {
                    if (p instanceof OAuth2AuthenticationToken) {
                        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) p;
                        OAuth2User oauth2User = oauth2Token.getPrincipal();
                        
                        // Extract username
                        String username = oauth2User.getName();
                        
                        // Extract roles
                        List<String> roles = oauth2Token.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList());
                        
                        // Extract additional attributes from OIDC user
                        String email = null;
                        String name = null;
                        
                        if (oauth2User instanceof OidcUser) {
                            OidcUser oidcUser = (OidcUser) oauth2User;
                            email = oidcUser.getEmail();
                            name = oidcUser.getFullName();
                            
                            // Fallback to preferred_username if name is not available
                            if (name == null || name.isEmpty()) {
                                name = oidcUser.getPreferredUsername();
                            }
                        } else {
                            // Fallback to attributes map
                            email = oauth2User.getAttribute("email");
                            name = oauth2User.getAttribute("name");
                            if (name == null || name.isEmpty()) {
                                name = oauth2User.getAttribute("preferred_username");
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
