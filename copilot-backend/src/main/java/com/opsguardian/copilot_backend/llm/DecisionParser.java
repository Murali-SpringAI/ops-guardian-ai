package com.opsguardian.copilot_backend.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsguardian.copilot_backend.model.AgentDecision;
import org.springframework.stereotype.Component;

@Component
public class DecisionParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentDecision parse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            return new AgentDecision(
                    root.path("action").asText("UNKNOWN"),
                    root.path("target").asText(null),
                    root.path("reason").asText("")
            );

        } catch (Exception e) {
            return new AgentDecision("UNKNOWN", null, "LLM parsing failed");
        }
    }
}
