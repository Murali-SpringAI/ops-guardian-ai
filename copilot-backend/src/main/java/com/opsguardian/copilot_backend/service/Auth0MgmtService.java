package com.opsguardian.copilot_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
public class Auth0MgmtService {

    private final WebClient webClient;

    private String cachedToken;
    private long expiryTime;

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.m2m.client-id}")
    private String clientId;

    @Value("${auth0.m2m.client-secret}")
    private String clientSecret;

    public Auth0MgmtService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String getMgmtApiToken() {

        //cache the token insteaf of making the call everytime.
        if (cachedToken != null && System.currentTimeMillis() < expiryTime) {
            return cachedToken;
        }

        Map<String, String> body = Map.of(
                "grant_type", "client_credentials",
                "client_id", clientId,
                "client_secret", clientSecret,
                "audience", "https://" + domain + "/api/v2/"
        );

        JsonNode response = webClient.post()
                .uri("https://" + domain + "/oauth/token")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(3))
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(500)))
                //.map(json -> json.get("access_token").asText())
                .block();


        cachedToken = response.get("access_token").asText();
        int expiresIn = response.get("expires_in").asInt();

        expiryTime = System.currentTimeMillis() + (expiresIn - 60) * 1000; // 1 min buffer

        return cachedToken;
    }
}
