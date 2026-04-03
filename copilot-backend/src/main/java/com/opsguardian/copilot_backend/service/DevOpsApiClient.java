package com.opsguardian.copilot_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class DevOpsApiClient {

    private final WebClient webClient;

    public DevOpsApiClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:8081").build();
    }

    public Object scaleService(String service, String token) {
        return webClient.post()
                .uri("api/scale")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("service", service))
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public Object restartService(String service, String token) {
        return webClient.post()
                .uri("api/restart")
                .bodyValue(Map.of("service", service))
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public Object fetchLogs(String service, String token) {
        return webClient.get()
                .uri("api/logs?service=" + service)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public Object fetchMetrics(String service, String token) {
        return webClient.get()
                .uri("api/metrics?service=" + service)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
}
