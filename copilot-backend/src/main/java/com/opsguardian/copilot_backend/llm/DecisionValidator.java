package com.opsguardian.copilot_backend.llm;

import com.opsguardian.copilot_backend.model.AgentDecision;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DecisionValidator {

    private static final Set<String> VALID_ACTIONS = Set.of(
            "RESTART_SERVICE",
            "SCALE_SERVICE",
            "FETCH_LOGS",
            "FETCH_METRICS",
            "CREATE_GITHUB_ISSUE",
            "LIST_GITHUB_REPOS"
    );

    private static final Set<String> ALLOWED_SERVICES = Set.of(
            "checkout-service",
            "payment-service",
            "user-service"
    );

    public boolean isValid(AgentDecision decision) {
        return decision != null
                && decision.getAction() != null
                && VALID_ACTIONS.contains(decision.getAction());
    }
}
