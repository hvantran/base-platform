# Spring Cloud Gateway with Keycloak OAuth2 Authentication

API Gateway service implementing OAuth2 authentication and authorization using Keycloak as the identity provider.

## Architecture Overview

```
┌─────────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Browser   │─────▶│   Gateway    │─────▶│   Keycloak   │      │   Backend    │
│   (User)    │◀─────│  (Port 6081) │◀─────│ (Port 6080)  │      │ Services     │
└─────────────┘      └──────────────┘      └──────────────┘      └──────────────┘
                            │                                            ▲
                            │  JWT Token Relay                           │
                            └────────────────────────────────────────────┘
```

## Features

- **OAuth2 Login**: Web-based authentication flow with Keycloak
- **JWT Token Relay**: Forwards access tokens to backend services
- **Session Management**: Web session for OAuth2 authentication
- **CORS Support**: Cross-origin requests from UI applications
- **Route Management**: Dynamic routing with path rewriting
- **Security**: Role-based access control with JWT validation

## OAuth2 Authentication Flow

### 1. Initial Request (Unauthenticated)

```
User → Gateway (/) → Redirect to Keycloak Login
```

- User accesses Gateway without authentication
- Gateway detects unauthenticated request
- Redirects to Keycloak login page

### 2. Authentication with Keycloak

```
User → Keycloak Login → Enter credentials → Keycloak validates → Generates tokens
```

- User enters username and password
- Keycloak validates credentials
- Generates ID Token, Access Token, and Refresh Token

### 3. Callback and Session Creation

```
Keycloak → Gateway (/login/oauth2/code/keycloak) → Create session → Redirect to UI
```

- Keycloak redirects back to Gateway callback URL
- Gateway exchanges authorization code for tokens
- Stores tokens in server-side session
- Redirects user to UI application

### 4. Authenticated API Requests

```
UI → Gateway (/api/**) 
   → Gateway validates session
   → Gateway adds JWT token to request (TokenRelay filter)
   → Forwards to backend service
   → Backend validates JWT
   → Returns response
```

## Configuration

### Environment Variables

#### Development (localhost)

```yaml
KEYCLOAK_ISSUER_URI: http://localhost:6080/realms/pman-realm
KEYCLOAK_CLIENT_SECRET: your-client-secret-here
APP_UI_URL: http://localhost:6084
```

#### Production (Docker)

```yaml
KEYCLOAK_AUTHORIZATION_URI: http://localhost:6080/realms/pman-realm/protocol/openid-connect/auth
KEYCLOAK_TOKEN_URI: http://keycloak:8080/realms/pman-realm/protocol/openid-connect/token
KEYCLOAK_USER_INFO_URI: http://keycloak:8080/realms/pman-realm/protocol/openid-connect/userinfo
KEYCLOAK_JWK_SET_URI: http://keycloak:8080/realms/pman-realm/protocol/openid-connect/certs
KEYCLOAK_CLIENT_SECRET: <secret-from-keycloak>
APP_UI_URL: http://localhost:6084
```

**Note**: Production uses explicit endpoint URIs instead of `issuer-uri` to avoid Docker container startup issues when Keycloak is not immediately available.

### Route Configuration

Routes are defined per profile (dev/prod):

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: action.manager.prod
          uri: http://action-manager-backend:8082
          predicates:
            - Path=/api/action-manager/**
          filters:
            - RewritePath=/api/action-manager(?<segment>/?.*), /action-manager-backend$\{segment}
            - TokenRelay=
```

#### Route Components

- **Predicate**: Matches incoming requests (e.g., `/api/action-manager/**`)
- **RewritePath Filter**: Transforms path before forwarding (adds service context path)
- **TokenRelay Filter**: Adds OAuth2 access token to forwarded requests

## Security Configuration

### OAuth2 Client Configuration

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: pman-client
            client-secret: ${KEYCLOAK_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            scope: openid,profile,email
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
```

### OAuth2 Resource Server Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${KEYCLOAK_JWK_SET_URI}
```

Gateway acts as both:
- **OAuth2 Client**: For browser-based login flow
- **OAuth2 Resource Server**: For validating JWT tokens from requests

### Security Rules

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
    .requestMatchers("/login", "/logout", "/oauth2/**", "/login/**").permitAll()
    .requestMatchers("/api/**").authenticated()
    .anyRequest().authenticated()
)
```

## CORS Configuration

Cross-Origin Resource Sharing (CORS) is configured to allow requests from the UI application:

```java
@Bean
public CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.addAllowedOrigin("http://localhost:6084");  // UI origin
    corsConfig.addAllowedMethod("*");
    corsConfig.addAllowedHeader("*");
    corsConfig.setAllowCredentials(true);
    // ...
}
```

**Important**: `allowCredentials: true` is required for session cookies to work with cross-origin requests.

## Backend Services Integration

Backend services must:

1. **Validate JWT tokens**: Use OAuth2 Resource Server configuration
2. **Extract roles from JWT**: Read from `realm_access.roles` claim
3. **Enforce authorization**: Use Spring Security method-level or HTTP security

### Backend Security Configuration Example

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/v1/actions/**").hasAnyRole("ACTION_VIEWER", "ACTION_MANAGER", "ADMIN")
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        )
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
    return http.build();
}
```

## Keycloak Configuration

### Realm Setup

1. **Create Realm**: `pman-realm`
2. **Create Client**: `pman-client`
   - Client Protocol: `openid-connect`
   - Access Type: `confidential`
   - Valid Redirect URIs: `http://localhost:6081/login/oauth2/code/keycloak`
   - Web Origins: `http://localhost:6081`

### Client Configuration

- **Client Authentication**: ON (confidential client)
- **Authorization**: OFF (not needed for this use case)
- **Standard Flow**: Enabled (authorization code flow)
- **Direct Access Grants**: Disabled (use standard flow only)

### Realm Roles

Create roles for backend services:
- `admin`: Full access to all resources
- `action-manager`: Create, update, pause, resume actions
- `action-viewer`: Read-only access to actions

### User Configuration

Assign realm roles to users in **Role Mappings** tab.

## Running the Application

### Development Mode

```bash
# Build Gateway
mvn clean package

# Run with dev profile
java -jar target/spring-cloud-gateway-app-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### Docker Mode

```bash
# Build Docker image
mvn clean package dockerfile:build

# Run with docker-compose
docker-compose -f docker-compose.test.yml up -d gateway
```

## Monitoring and Debugging

### Actuator Endpoints

Available at `/actuator`:
- `/actuator/health`: Health status
- `/actuator/info`: Application information
- `/actuator/prometheus`: Prometheus metrics
- `/actuator/gateway/routes`: View all configured routes

### Logging

Production logging levels:
- Gateway: `INFO`
- Security: `INFO`

For debugging, set `TRACE` level for specific loggers:
```yaml
logging:
  level:
    org.springframework.cloud.gateway: TRACE
    org.springframework.security: DEBUG
```

## Troubleshooting

### Issue: 401 Unauthorized from backend

**Cause**: JWT token issuer mismatch or missing roles

**Solution**:
1. Backend should use `jwk-set-uri` without `issuer-uri` validation
2. Verify user has proper roles in Keycloak
3. Check JWT token contains `realm_access.roles` claim

### Issue: CORS errors from UI

**Cause**: CORS misconfiguration or missing credentials

**Solution**:
1. Verify UI origin is in `allowedOrigins`
2. Ensure `allowCredentials: true` in CORS config
3. UI must send `credentials: 'include'` in fetch requests

### Issue: Redirect loop after login

**Cause**: OAuth2LoginSuccessHandler misconfiguration

**Solution**:
- Verify `APP_UI_URL` environment variable is set correctly
- Check OAuth2LoginSuccessHandler redirects to UI, not Gateway

### Issue: Gateway routes not loading

**Cause**: Spring Cloud version incompatibility

**Solution**:
- Verify Spring Cloud version matches Spring Boot version
- Spring Boot 3.2.x requires Spring Cloud 2023.0.x (Leyton)

## Dependencies

### Core Dependencies

```xml
<!-- Spring Cloud Gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- OAuth2 Client for login flow -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- OAuth2 Resource Server for JWT validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- WebFlux (reactive) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Actuator for monitoring -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Version Compatibility

- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0 (Leyton release train)
- **Java**: 21

**Important**: Spring Cloud Gateway requires WebFlux (reactive) architecture. Do not include servlet-based dependencies (e.g., `spring-boot-starter-web`).

## Related Documentation

- [Spring Cloud Gateway Documentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

## Related Issue

- GitHub Issue: [hvantran/project-management#187](https://github.com/hvantran/project-management/issues/187)
