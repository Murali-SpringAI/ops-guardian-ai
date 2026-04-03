package com.opsguardian.copilot_backend.controller;


import com.opsguardian.copilot_backend.service.GithubService;
import com.opsguardian.copilot_backend.service.GithubTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    private final GithubTokenService githubTokenService;
    private final GithubService githubService;

    public GithubController(GithubTokenService githubTokenService, GithubService githubService) {
        this.githubTokenService = githubTokenService;
        this.githubService = githubService;
    }

    @GetMapping("/repos")
    public ResponseEntity<?> getRepos(@AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject(); // e.g., "github|61990734"
        String githubToken = githubTokenService.fetchGithubAccessToken(userId);

        if (githubToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("GitHub token not available");
        }

        return ResponseEntity.ok(githubService.listRepos(githubToken));
    }
}
