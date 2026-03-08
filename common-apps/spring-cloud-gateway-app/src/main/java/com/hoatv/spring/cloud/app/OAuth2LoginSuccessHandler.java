package com.hoatv.spring.cloud.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Custom authentication success handler that redirects users to the UI application
 * after successful OAuth2 authentication and saves the SecurityContext to the session.
 * 
 * This handler extracts the original request URI from the saved request (if available)
 * and redirects back to it. If no saved request exists, it redirects to the default
 * UI home page.
 * 
 * Related to hvantran/project-management#187
 */
@Component
public class OAuth2LoginSuccessHandler implements ServerAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    
    private final WebSessionServerSecurityContextRepository securityContextRepository;

    @Value("${app.ui.url:http://localhost:6084}")
    private String uiUrl;
    
    public OAuth2LoginSuccessHandler(WebSessionServerSecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, 
                                               Authentication authentication) {
        logger.info("OAuth2 login successful for user: {}", authentication.getName());
        
        // Create SecurityContext and save it to the session
        SecurityContext securityContext = new SecurityContextImpl(authentication);
        
        return securityContextRepository.save(webFilterExchange.getExchange(), securityContext)
                .then(webFilterExchange.getExchange().getSession())
                .flatMap(session -> {
                    // Check if there's a saved request URI
                    String redirectUri = session.getAttribute("REDIRECT_URI");
                    
                    if (redirectUri != null && !redirectUri.isEmpty()) {
                        logger.info("Redirecting to saved URI: {}", redirectUri);
                        session.getAttributes().remove("REDIRECT_URI");
                        webFilterExchange.getExchange().getResponse()
                                .setStatusCode(org.springframework.http.HttpStatus.FOUND);
                        webFilterExchange.getExchange().getResponse().getHeaders()
                                .setLocation(URI.create(redirectUri));
                    } else {
                        // Default redirect to UI home page
                        logger.info("No saved request, redirecting to UI home: {}", uiUrl);
                        webFilterExchange.getExchange().getResponse()
                                .setStatusCode(org.springframework.http.HttpStatus.FOUND);
                        webFilterExchange.getExchange().getResponse().getHeaders()
                                .setLocation(URI.create(uiUrl));
                    }
                    
                    return webFilterExchange.getExchange().getResponse().setComplete();
                });
    }
}
