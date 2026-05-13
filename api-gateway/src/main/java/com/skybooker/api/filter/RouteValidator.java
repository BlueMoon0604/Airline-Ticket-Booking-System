package com.skybooker.api.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/auth/validate",
            "/auth/oauth2/login-info",
            "/airlines",
            "/airports",
            "/flights/search",
            "/flights/search/round-trip",
            "/flights",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/actuator/health",
            "/gateway/info"
    );

    public final Predicate<ServerHttpRequest> isSecured =
            request -> OPEN_API_ENDPOINTS
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().startsWith(uri));
}
