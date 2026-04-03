package com.opsguardian.copilot_backend.llm;

import com.opsguardian.copilot_backend.model.AgentDecision;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LlmService {

    private final WebClient webClient;
    private final PromptBuilder promptBuilder;
    private final DecisionParser parser;
    private final DecisionValidator validator;

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    private static final int MAX_RETRIES = 2;

    public AgentDecision decide(String userInput) {

        System.out.println("Calling LLM...");
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {

            try {
                String response = callLlm(userInput);
                System.out.println("🔥 LLM RAW RESPONSE: " + response);

                AgentDecision decision = parser.parse(response);

                if (validator.isValid(decision)) {
                    return decision;
                }

            } catch (Exception ignored) {
                System.out.println("exceltion from LLM: " + ignored.getMessage());
            }
        }

        // 🔥 Fallback
        return new AgentDecision(
                "UNKNOWN",
                null,
                "LLM failed or unsafe decision"
        );
    }


    private String callLlm(String userInput) {

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", promptBuilder.buildSystemPrompt()),
                        Map.of("role", "user", "content", userInput)
                ),
                "temperature", 0.2
        );

        System.out.println("Key: " + apiKey);
        return webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .map(res -> {
                    List<Map<String, Object>> choices =
                            (List<Map<String, Object>>) res.get("choices");

                    Map<String, Object> message =
                            (Map<String, Object>) choices.get(0).get("message");

                    return (String) message.get("content");
                })
                .block();
    }
}
