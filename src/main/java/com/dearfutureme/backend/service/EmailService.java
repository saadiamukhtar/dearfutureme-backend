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

        String safeNote = capsule.getNote().replace("%", "%%");

        String videoSection = "";
        if (capsule.getVideoUrl() != null && !capsule.getVideoUrl().isBlank()) {
            videoSection = """
                    <table width="100%%" cellpadding="0" cellspacing="0">
                      <tr>
                        <td style="padding:20px 56px 12px;text-align:center;">
                          <a href="%s"
                             style="display:inline-block;background:#1e130a;color:#f5ede0;
                                    text-decoration:none;padding:15px 42px;border-radius:2px;
                                    font-family:'Lora',Georgia,serif;font-size:14px;
                                    letter-spacing:0.07em;">
                            &#9654;&nbsp; Watch Your Video Message
                          </a>
                        </td>
                      </tr>
                    </table>
                    """.formatted(capsule.getVideoUrl());
        }

        String aiSection = "";
        if (capsule.getAiSummary() != null && !capsule.getAiSummary().isBlank()) {

            String aiText = capsule.getAiSummary()
                    .replace("\n\n", "</p><p style=\"font-family:'Lora',Georgia,serif;"
                            + "color:#264a30;font-size:15px;line-height:1.85;margin:12px 0 0;\">")
                    .replace("\n", "<br>");

            aiSection = """
                    <table width="100%%" cellpadding="0" cellspacing="0">
                      <tr>
                        <td style="padding:20px 56px 12px;">
                          <div style="background:#f2f8f2;border:1px solid #b0ccb0;
                                      border-radius:4px;padding:24px 28px;">
                            <p style="font-family:'Lora',Georgia,serif;font-size:11px;
                                      font-weight:500;color:#4a8060;letter-spacing:0.18em;
                                      text-transform:uppercase;margin:0 0 12px;">
                              A reflection from your AI companion
                            </p>
                            <p style="font-family:'Lora',Georgia,serif;color:#264a30;
                                      font-size:15px;line-height:1.85;margin:0;">%s</p>
                          </div>
                        </td>
                      </tr>
                    </table>
                    """.formatted(aiText);
        }

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>A message from your past self</title>
                </head>
                <body>

                <!-- template shortened here for readability -->

                </body>
                </html>
                """.formatted(
                createdFormatted,
                safeNote,
                videoSection,
                aiSection,
                createdFormatted
        );
    }
}