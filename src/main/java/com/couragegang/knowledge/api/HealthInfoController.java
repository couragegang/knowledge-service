package com.couragegang.knowledge.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import java.util.Map;

@Controller
public final class HealthInfoController {

    @Get("/")
    public Map<String, String> root() {
        return Map.of(
                "service", "knowledge-service",
                "health", "/v1/knowledge/health",
                "connectors", "/v1/knowledge/connectors",
                "search", "/v1/knowledge/search");
    }
}
