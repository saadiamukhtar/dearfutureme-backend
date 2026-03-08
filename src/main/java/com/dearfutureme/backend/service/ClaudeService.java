package com.dearfutureme.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ClaudeService {

    private final RestTemplate restTemplate;

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    @Value("${anthropic.model}")
    private String model;

    public ClaudeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls the Claude API to generate a warm AI reflection summary
     * based on the user's note to their future self.
     */
    public String generateReflection(String note) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            String prompt = buildPrompt(note);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", 1024,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> content =
                        (List<Map<String, Object>>) response.getBody().get("content");
                if (content != null && !content.isEmpty()) {
                    return (String) content.get(0).get("text");
                }
            }

            log.warn("Claude API returned unexpected response: {}", response.getStatusCode());
            return null;

        } catch (Exception e) {
            log.error("Failed to generate AI reflection from Claude: {}", e.getMessage());
            return null;
        }
    }

    private String buildPrompt(String note) {
        return """
                You are a warm, empathetic AI assistant for an app called "Dear Future Me" \
                — a digital time capsule where people write heartfelt messages to their future selves.

                A user has written the following message to their future self:

                ---
                %s
                ---

                Please generate a thoughtful, warm reflection (2-3 paragraphs) that:
                1. Captures the emotional essence and themes of their message
                2. Gently highlights what this moment in time might mean to their future self
                3. Offers encouragement and celebrates the beautiful act of writing across time

                Write in second person ("you"), addressing the future self directly. \
                Keep the tone warm, human, and uplifting — never generic or robotic.
                """.formatted(note);
    }
}
