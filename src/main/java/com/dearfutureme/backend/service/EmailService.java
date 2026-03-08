package com.dearfutureme.backend.service;

import com.dearfutureme.backend.entity.Capsule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate;

    @Value("${RESEND_API_KEY:${resend.api.key:}}")
    private String apiKey;

    @Value("${resend.api.url:https://api.resend.com/emails}")
    private String apiUrl;

    @Value("${RESEND_FROM_EMAIL:noreply@sadiamukhtar.in}")
    private String fromEmail;

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        log.info("EmailService ready — from: '{}', apiUrl: {}, apiKey set: {}",
                fromEmail, apiUrl, !apiKey.isBlank());
    }

    public boolean sendCapsuleEmail(Capsule capsule) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            String htmlBody = buildEmailHtml(capsule);

            Map<String, Object> requestBody = new HashMap<>();

            requestBody.put("from", "Dear Future Me <" + fromEmail + ">");
            requestBody.put("to", List.of(capsule.getEmail()));
            requestBody.put("subject", "A message from your past self has arrived");
            requestBody.put("html", htmlBody);

            log.info("Sending email via Resend -> from: {}, to: {}", fromEmail, capsule.getEmail());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent successfully to {} for capsule id={}", capsule.getEmail(), capsule.getId());
                return true;
            }

            log.warn("Resend API returned status {} for capsule id={}", response.getStatusCode(), capsule.getId());
            return false;

        } catch (Exception e) {
            log.error("Failed to send email for capsule id={}: {}", capsule.getId(), e.getMessage());
            return false;
        }
    }

    private String buildEmailHtml(Capsule capsule) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        String createdFormatted = capsule.getCreatedAt().format(fmt);

        String videoSection = "";
        if (capsule.getVideoUrl() != null && !capsule.getVideoUrl().isBlank()) {

            videoSection = """
                    <div style="margin: 28px 0;">
                      <h3 style="color:#6b7280; font-size:13px; font-weight:600;
                                 letter-spacing:0.08em; text-transform:uppercase; margin-bottom:12px;">
                        Your Video
                      </h3>
                      <a href="%s"
                         style="display:inline-block; background:#4f46e5; color:#fff;
                                text-decoration:none; padding:12px 24px; border-radius:8px;
                                font-weight:600; font-size:15px;">
                        Watch Your Video
                      </a>
                    </div>
                    """.formatted(capsule.getVideoUrl());
        }

        String aiSection = "";
        if (capsule.getAiSummary() != null && !capsule.getAiSummary().isBlank()) {

            String aiText = capsule.getAiSummary()
                    .replace("\n\n", "</p><p style=\"margin-bottom:16px;\">")
                    .replace("\n", "<br>");

            aiSection = """
                    <div style="background:#f0fdf4; border-left:4px solid #22c55e;
                                border-radius:0 8px 8px 0; padding:20px 24px; margin:28px 0;">
                      <h3 style="color:#15803d; font-size:13px; font-weight:600;
                                 letter-spacing:0.08em; text-transform:uppercase; margin:0 0 12px;">
                        A reflection from your AI companion
                      </h3>
                      <p style="color:#166534; font-size:15px; line-height:1.7;">%s</p>
                    </div>
                    """.formatted(aiText);
        }

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial, sans-serif; background:#f9fafb; padding:40px">

                <h2>A message from your past self</h2>

                <p><b>Written on:</b> %s</p>

                <h3>Your Message</h3>
                <p>%s</p>

                %s

                %s

                <p style="margin-top:40px; color:#888;">
                Dear Future Me — Your digital time capsule
                </p>

                </body>
                </html>
                """.formatted(
                createdFormatted,
                capsule.getNote(),
                videoSection,
                aiSection
        );
    }
}