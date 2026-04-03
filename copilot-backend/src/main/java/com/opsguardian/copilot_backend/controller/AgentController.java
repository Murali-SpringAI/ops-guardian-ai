package com.opsguardian.copilot_backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opsguardian.copilot_backend.llm.LlmService;
import com.opsguardian.copilot_backend.model.AgentCommand;
import com.opsguardian.copilot_backend.model.AgentDecision;
import com.opsguardian.copilot_backend.model.AgentRequest;
import com.opsguardian.copilot_backend.model.AgentResponse;
import com.opsguardian.copilot_backend.service.AgentService;
import com.opsguardian.copilot_backend.service.AiAgentService;
import com.opsguardian.copilot_backend.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final LlmService llmService;
    private final AuditService auditService;

    @PostMapping("/command")
    public ResponseEntity<AgentResponse> handleCommand(@RequestBody AgentRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        System.out.println("process request...");

        String userId = jwt.getSubject();

        AgentResponse response = null;
        try {
            response = agentService.processCommand(request.getCommand(),
                    jwt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/execute")
    public ResponseEntity<AgentResponse> execute(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal Jwt jwt
    ) {

        // 🔍 DEBUG JWT
        System.out.println("==== JWT DEBUG START ====");
        System.out.println("Subject: " + jwt.getSubject());
        System.out.println("Issuer: " + jwt.getIssuer());
        System.out.println("Audience: " + jwt.getAudience());
        System.out.println("Permissions: " + jwt.getClaim("permissions"));
        System.out.println("Scopes: " + jwt.getClaim("scope"));
        System.out.println("==== JWT DEBUG END ====");

        String command = payload.get("command");

        System.out.println("Execute: " + command);
        // 🧠 Step 1: LLM Decision
        AgentDecision decision = llmService.decide(command);

        if (decision == null || decision.getAction() == null) {
            return ResponseEntity.ok(new AgentResponse(
                    "FAILED",
                    "Could not հասկանդ request",
                    null
            ));
        }

        System.out.println("LLM Action: " + decision.getAction());
        System.out.println("LLM target: " + decision.getTarget());
        System.out.println("LLM reason: " + decision.getReason());

        // 🔐 Step 2: Execute
        // 🚨 Step 2: Check if action needs confirmation
        if (List.of("RESTART_SERVICE", "SCALE_SERVICE").contains(decision.getAction())) {

            return ResponseEntity.ok(new AgentResponse(
                    "CONFIRMATION_REQUIRED",
                    "⚠️ Are you sure you want to " + decision.getAction() +
                            " on " + decision.getTarget() + "?",
                    Map.of(
                            "action", decision.getAction(),
                            "target", decision.getTarget()
                    )
            ));
        }

// ✅ Safe actions execute directly
        Object result = agentService.executeAction(
                jwt.getSubject(),
                decision.getAction(),
                decision.getTarget()
        );

        return ResponseEntity.ok(new AgentResponse(
                "SUCCESS",
                "Executed " + decision.getAction() + " on " + decision.getTarget(),
                Map.of("result", result)
        ));
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }

    @PostMapping("/confirm")
    public ResponseEntity<AgentResponse> confirm(
            @RequestParam String action,
            @RequestParam String target,
            @AuthenticationPrincipal Jwt jwt
    ) {

        // ✅ Normalize target
        target = target.replace(" ", "-").toLowerCase();

        // 🚨 Allow only confirmable actions
        if (!List.of("RESTART_SERVICE", "SCALE_SERVICE").contains(action)) {
            throw new RuntimeException("Invalid confirmation action");
        }

        // 🔐 Re-check permission
        agentService.validatePermission(jwt, action);

        // 📝 Audit log
        auditService.log(
                jwt.getSubject(),
                action,
                "User confirmed execution for " + target
        );

        Object result = agentService.executeAction(
                jwt.getSubject(),
                action,
                target
        );

        return ResponseEntity.ok(new AgentResponse(
                "SUCCESS",
                "✅ Successfully executed " + action + " on " + target,
                Map.of("result", result)
        ));
    }
}
