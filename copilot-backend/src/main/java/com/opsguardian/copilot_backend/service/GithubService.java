package com.opsguardian.copilot_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class GithubService {

    private final WebClient webClient;
    private final RestTemplate restTemplate;

    private static final String GITHUB_API = "https://api.github.com";

    // 🔥 CREATE ISSUE
    public Object createIssue(String repo, String token) {

        String url = "https://api.github.com/repos/" + repo + "/issues";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("title", "🚨 Auto-created issue by OpsGuardian");
        body.put("body", "Detected issue via AI agent");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return "🐙 ✅ Issue created: " + response.getBody().get("html_url");

        } catch (Exception ex) {
            return "🐙 ❌ Failed to create GitHub issue: " + ex.getMessage();
        }
    }

    // 🔥 LIST REPOS
    public Object listRepos(String token) {

        String url = "https://api.github.com/user/repos?visibility=all&per_page=100";

        System.out.println("GitHub Token: " + token.substring(0, 10));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
        );

        List<Map<String, Object>> repos = response.getBody();

        return repos.stream()
                .map(r -> "• " + r.get("full_name"))
                .toList();
    }
}
