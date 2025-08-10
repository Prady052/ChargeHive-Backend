package com.charginhive.apigateway.filter;


import com.charginhive.apigateway.config.RouterValidator;
import com.charginhive.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private RouterValidator routerValidator;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.warn("Request URI : {}", request.getURI());
        // Check if the endpoint is secured
        if (routerValidator.isSecured(request)) {
            // Check for Authorization header
            if (this.isAuthMissing(request)) {
                log.warn("Authorization header is missing for secured endpoint: {}", request.getURI());
                return this.onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
            }

            final String token = this.getAuthHeader(request);

            //Validate the JWT
            try {
                jwtUtil.validateToken(token);
            } catch (Exception e) {
                log.error("Invalid authorization token. Error: {}", e.getMessage());
                return this.onError(exchange, "Authorization failed: Invalid Token", HttpStatus.UNAUTHORIZED);
            }

            Claims claims = jwtUtil.getClaims(token);

            //Check for required role
            if (!hasRequiredRole(request, (List<String>) claims.get("authorities"))) {
                log.warn("User does not have required role to access {}. Authorities: {}", request.getURI().getPath(), claims.get("authorities"));
                return this.onError(exchange, "Access Denied: Insufficient permissions", HttpStatus.FORBIDDEN);
            }


            // Add user ID to request header
            // changes made here
            Long userId = claims.get("user_id", Long.class);
            log.debug("Authenticated user: {}, forwarding request to: {}", userId, request.getURI());


            //changes made here
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId.toString())
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        // Let public endpoints pass throughssss
        return chain.filter(exchange);
    }

    /**
     * Defines the order of the filter. -1 means it runs with high priority.
     */
    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private String getAuthHeader(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getOrEmpty("Authorization").get(0);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    /**
     * Checks if the user has the required role to access the requested path.
     */
    private boolean hasRequiredRole(ServerHttpRequest request, List<String> roles) {
        String path = request.getURI().getPath();
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        if (path.startsWith("/stations")) {

            if (path.contains("/unapproved")) {
                return roles.contains("ROLE_ADMIN");
            }

            if (request.getMethod() != HttpMethod.GET) {
                return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_OPERATOR");
            }
        }


        if (path.startsWith("/admin")) {
            return roles.contains("ROLE_ADMIN");
        }

        return true;
    }
}

