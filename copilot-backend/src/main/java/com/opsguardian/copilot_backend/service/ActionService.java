package com.opsguardian.copilot_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ActionService {

    private final WebClient webClient;

    public ActionService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String deploy(String service, String token) {

        if (token == null) {
            return "❌ No GitHub token found.";
        }

        return webClient.post()
                .uri("http://localhost:8081/restart-service")
                .bodyValue(Map.of("service", service))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getLogs(String service) {
        return webClient.get()
                .uri("http://localhost:8081/logs?service=" + service)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String scale(String service) {
        return webClient.post()
                .uri("http://localhost:8081/scale-service")
                .bodyValue(Map.of("service", service))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
