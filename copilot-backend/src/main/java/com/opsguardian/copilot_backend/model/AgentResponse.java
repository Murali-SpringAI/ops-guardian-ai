package com.opsguardian.copilot_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentResponse {

    private String status;
    private String message;
    private Object data;
}
