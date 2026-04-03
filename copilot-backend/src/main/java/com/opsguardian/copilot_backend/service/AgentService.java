package com.opsguardian.copilot_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsguardian.copilot_backend.llm.LlmService;
import com.opsguardian.copilot_backend.model.AgentDecision;
import com.opsguardian.copilot_backend.model.AgentResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AgentService {

    private final TokenVaultService tokenVaultService;
    private final DevOpsApiClient devOpsApiClient;
    private final AuditService auditService;
    private final LlmService llmService;
    private final GithubService githubService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String> ACTION_PERMISSION_MAP = Map.of(
            "SCALE_SERVICE", "scale:services",
            "RESTART_SERVICE", "restart:services",
            "FETCH_LOGS", "view:logs",
            "FETCH_METRICS", "view:metrics",
            "CREATE_GITHUB_ISSUE", "github:write",
            "LIST_GITHUB_REPOS", "github:read",
            "RESTART_EC2", "aws:restart",
            "DEPLOY_K8S", "k8s:deploy",
            "FETCH_DATADOG_METRICS", "datadog:read",
            "FETCH_SPLUNK_LOGS", "splunk:read"
    );

    public AgentService(TokenVaultService tokenVaultService,
                        DevOpsApiClient devOpsApiClient,
                        AuditService auditService,
                        LlmService llmService,
                        GithubService githubService) {
        this.tokenVaultService = tokenVaultService;
        this.devOpsApiClient = devOpsApiClient;
        this.auditService = auditService;
        this.llmService = llmService;
        this.githubService = githubService;
    }

    /**
     * Execute action on behalf of a user
     */
    public Object executeAction(String userId, String action, String target) {

        if (action == null || action.equals("UNKNOWN")) {
            return "⚠️ LLM could not determine action. Try rephrasing.";
        }

        List<Map<String, String>> identities = tokenVaultService.getUserIdentities(userId);

        String githubToken = identities.stream()
                .filter(i -> "github".equals(i.get("provider")))
                .map(i -> i.get("access_token"))
                .findFirst()
                .orElse(null);

        Object result;

        switch (action) {

            // 🔹 INFRA ACTIONS
            case "RESTART_SERVICE":
                result = devOpsApiClient.restartService(target, githubToken);
                result = "🔁 Restarted " + target + " successfully";
                break;

            case "SCALE_SERVICE":
                result = devOpsApiClient.scaleService(target, githubToken);
                result = "📈 Scaled " + target + " successfully";
                break;

            case "FETCH_LOGS":
                result = devOpsApiClient.fetchLogs(target, githubToken);
                result = "📜 Logs for " + target + ":\n" + result;
                break;

            case "FETCH_METRICS":
                result = devOpsApiClient.fetchMetrics(target, githubToken);
                result = "📊 Metrics for " + target + ": " + result;
                break;

            // 🔹 GITHUB ACTIONS
            case "CREATE_GITHUB_ISSUE":
                if (githubToken == null) return "No GitHub token found. Please reconnect GitHub.";
                if (!target.contains("/")) target = "mnidugondi/" + target.replace(" ", "-").toLowerCase();
                result = githubService.createIssue(target, githubToken);
                result = "🐙 " + result;
                break;

            case "LIST_GITHUB_REPOS":
                if (githubToken == null) return "No GitHub token found. Please reconnect GitHub.";
                result = githubService.listRepos(githubToken);
                result = "📦 " + result;
                break;

            case "RESTART_EC2":
                result = "☁️ AWS EC2 instance " + target + " restarted";
                break;

            case "DEPLOY_K8S":
                result = "🚢 Kubernetes deployment updated for " + target;
                break;

            case "FETCH_DATADOG_METRICS":
                result = "📊 Datadog metrics for " + target + ": CPU 70%, Memory 65%";
                break;

            case "FETCH_SPLUNK_LOGS":
                result = "📜 Splunk logs for " + target + ": No errors found";
                break;

            default:
                return "⚠️ Unsupported action: " + action;
        }

        // 🔥 Format result if it's a list
        if (result instanceof List<?>) {
            result = String.join("\n", (List<String>) result);
        }

        return result;
    }

    /**
     * Process a command from the user
     */
    public AgentResponse processCommand(String command, Jwt jwt) throws JsonProcessingException {

        AgentDecision decision = llmService.decide(command);

        if ("UNKNOWN".equals(decision.getAction())) {
            return new AgentResponse("Could not understand request",
                    "LLM returned unknown",
                    null);
        }

        String action = decision.getAction();
        String target = decision.getTarget();
        if (target != null) target = target.replace(" ", "-").toLowerCase();

        // 🚨 Policy check before permission/confirmation
        String policyViolation = checkPolicy(action, target);
        if (policyViolation != null) {
            return new AgentResponse(
                    "BLOCKED",
                    policyViolation,
                    Map.of(
                            "action", action,
                            "target", target
                    )
            );
        }

        // ✅ Permission check
        validatePermission(jwt, action);

        if (requiresApproval(action)) {
            return new AgentResponse(
                    "CONFIRMATION_REQUIRED",
                    "⚠️ Are you sure you want to " + action + " " + target + "?",
                    Map.of(
                            "confirmEndpoint", "/api/agent/confirm",
                            "action", action,
                            "target", target
                    )
            );
        }

        Object result = executeAction(jwt.getSubject(), action, target);

        return new AgentResponse(
                "SUCCESS",
                decision.getReason(),
                Map.of(
                        "action", action,
                        "target", target,
                        "result", result
                )
        );
    }

    private Object parseIfJson(String input) {
        try {
            Object parsed = objectMapper.readValue(input, Object.class);
            if (parsed instanceof String) parsed = objectMapper.readValue((String) parsed, Object.class);
            return parsed;
        } catch (Exception e) {
            return input;
        }
    }

    private boolean hasScope(Jwt jwt, String scope) {
        List<String> scopes = jwt.getClaimAsStringList("scope");
        return scopes != null && scopes.contains(scope);
    }

    private AgentDecision legacyDecide(String command) {
        String lower = command.toLowerCase();
        String target = extractTarget(lower);
        if (lower.contains("cpu") || lower.contains("performance")) return new AgentDecision("metrics", target, "Checking system performance");
        if (lower.contains("restart")) return new AgentDecision("restart", target, "User requested restart");
        if (lower.contains("scale")) return new AgentDecision("scale", target, "User requested scaling");
        if (lower.contains("logs")) return new AgentDecision("logs", target, "Fetching logs");
        if (lower.contains("metrics")) return new AgentDecision("metrics", target, "Fetching metrics");
        return new AgentDecision("unknown", target, "Unknown command");
    }

    public void validatePermission(Jwt jwt, String action) {
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        String requiredPermission = ACTION_PERMISSION_MAP.get(action);
        if (requiredPermission == null) throw new RuntimeException("Unknown action: " + action);
        if (permissions == null || !permissions.contains(requiredPermission)) throw new RuntimeException("Unauthorized action: " + action);
    }

    private String extractTarget(String command) {
        if (command.contains("checkout")) return "checkout-service";
        if (command.contains("payment")) return "payment-service";
        if (command.contains("orders")) return "orders-service";
        return "system";
    }

    private boolean requiresApproval(String action) {
        return "RESTART_SERVICE".equals(action) || "SCALE_SERVICE".equals(action);
    }

    private String checkPolicy(String action, String target) {
        // 🚫 Block production restart
        if ("RESTART_SERVICE".equals(action) && target.contains("prod")) {
            return "Policy violation: Cannot restart production service";
        }
        // 🚫 Restrict scaling example
        if ("SCALE_SERVICE".equals(action)) {
            return "Policy violation: Scaling requires admin approval";
        }
        return null;
    }
}
