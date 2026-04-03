package com.opsguardian.copilot_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GithubTokenService {

    private final WebClient webClient;
    private final Auth0MgmtService auth0MgmtService;

    public GithubTokenService(WebClient webClient, Auth0MgmtService auth0MgmtService) {
        this.webClient = webClient;
        this.auth0MgmtService = auth0MgmtService;
    }

    public String fetchGithubAccessToken(String userId) {
        String mgmtToken = auth0MgmtService.getMgmtApiToken();

        JsonNode userInfo = webClient.get()
                .uri("https://dev-frvjdwj3fq0gwfb7.us.auth0.com/api/v2/users/" + userId)
                .headers(h -> h.setBearerAuth(mgmtToken))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        for (JsonNode identity : userInfo.get("identities")) {
            if ("github".equals(identity.get("provider").asText())) {
                return identity.get("access_token").asText();
            }
        }
        return null;
    }
}
