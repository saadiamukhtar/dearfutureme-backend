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
public class GeminiService {

    private final RestTemplate restTemplate;

    @Value("${GEMINI_API_KEY:${gemini.api.key:}}")
    private String apiKey;

    @Value("${GEMINI_API_URL:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateReflection(String note) {
        try {

            String prompt = buildPrompt(note);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    ),
                    "generationConfig", Map.of(
                            "maxOutputTokens", 180,
                            "temperature", 0.7,
                            "topP", 0.9
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "?key=" + apiKey,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getBody() != null) {

                List<Map<String, Object>> candidates =
                        (List<Map<String, Object>>) response.getBody().get("candidates");

                if (candidates != null && !candidates.isEmpty()) {

                    Map<String, Object> content =
                            (Map<String, Object>) candidates.get(0).get("content");

                    List<Map<String, Object>> parts =
                            (List<Map<String, Object>>) content.get("parts");

                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }

            log.warn("Gemini API returned unexpected response");
            return null;

        } catch (Exception e) {
            log.error("Failed to generate AI reflection from Gemini: {}", e.getMessage());
            return null;
        }
    }

    private String buildPrompt(String note) {
        return """
A person wrote a message to their future self.

Message:
%s

Write a short warm reflection. write atleast 1 paragraph encouraging their future self.
Speak in second person ("you").
""".formatted(note);
    }
}