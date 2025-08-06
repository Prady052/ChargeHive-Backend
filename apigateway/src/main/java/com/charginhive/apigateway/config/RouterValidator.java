package com.charginhive.apigateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    //all public endpoints that do not require a JWT for access
    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            "/test/hello",
            "/eureka"
    );

    // A interface with sam(boolean test(T t)) to test if a given request is for a secured endpoint
//    public Predicate<ServerHttpRequest> isSecured =
//            request -> openApiEndpoints
//                    .stream()
//                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
    public boolean isSecured(ServerHttpRequest request) {
        return openApiEndpoints
                .stream()
                .noneMatch(uri -> request.getURI().getPath().contains(uri));
    }

}
