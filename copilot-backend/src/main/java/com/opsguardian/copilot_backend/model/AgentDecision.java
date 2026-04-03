package com.opsguardian.copilot_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentDecision {

    private String action;   // scale, restart, logs, metrics
    private String target;   // system, service-name
    private String reason;   // why this action

}
