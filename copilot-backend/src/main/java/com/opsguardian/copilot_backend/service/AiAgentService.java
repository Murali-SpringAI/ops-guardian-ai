package com.opsguardian.copilot_backend.service;

import com.opsguardian.copilot_backend.model.AgentCommand;
import org.springframework.stereotype.Service;

@Service
public class AiAgentService {

    private final TokenVaultService tokenVaultService;
    private final ActionService actionService;
    private final AuditService auditService;

    public AiAgentService(TokenVaultService tokenVaultService,
                          ActionService actionService,
                          AuditService auditService) {
        this.tokenVaultService = tokenVaultService;
        this.actionService = actionService;
        this.auditService = auditService;
    }

    public String handleCommand(String userId, AgentCommand command) {

        auditService.log(userId, command.getAction(), command.getTarget());

        switch (command.getAction()) {

            case "deploy":
                String token = tokenVaultService.getGithubToken(userId);
                return actionService.deploy(command.getTarget(), token);

            case "logs":
                return actionService.getLogs(command.getTarget());

            case "scale":
                return actionService.scale(command.getTarget());

            default:
                return "❌ Unknown action";
        }
    }

}
