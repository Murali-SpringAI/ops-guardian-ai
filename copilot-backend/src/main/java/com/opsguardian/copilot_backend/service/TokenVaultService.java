package com.opsguardian.copilot_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class TokenVaultService {

    private final WebClient webClient = WebClient.create();

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.m2m.client-id}")
    private String clientId;

    @Value("${auth0.m2m.client-secret}")
    private String clientSecret;

    /**
     * Get M2M Management API token from Auth0
     */
    public String getManagementApiToken() {
        Map<String, String> response = webClient.post()
                .uri("https://" + domain + "/oauth/token")
                .bodyValue(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "audience", "https://" + domain + "/api/v2/",
                        "grant_type", "client_credentials"
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (String) response.get("access_token");
    }

    /**
     * Fetch user identities (GitHub token, etc.)
     * If JWT is from M2M client, return demo token
     */
    public List<Map<String, String>> getUserIdentities(String userId) {
        // Hackathon-friendly fallback for client JWTs
        if (userId.endsWith("@clients")) {
            return List.of(
                    Map.of("provider", "github", "access_token", "demo-github-token")
            );
        }

        // Real user flow
        String token = getManagementApiToken();
        Map userProfile = webClient.get()
                .uri("https://" + domain + "/api/v2/users/" + userId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Extract identities
        return (List<Map<String, String>>) userProfile.get("identities");
    }

    //mxn
    public String getGithubToken(String userId){
        return null;
    }

}
