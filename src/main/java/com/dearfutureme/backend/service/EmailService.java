package com.dearfutureme.backend.service;

import com.dearfutureme.backend.entity.Capsule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate;

    // Checks RESEND_API_KEY env var first, then property file
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

    /**
     * Sends the time capsule delivery email via Resend API.
     */
    public boolean sendCapsuleEmail(Capsule capsule) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            String htmlBody = buildEmailHtml(capsule);

            Map<String, Object> requestBody = Map.of(
                    "from", "Dear Future Me <" + fromEmail + ">",
                    "to", capsule.getEmail(),
                    "subject", "A message from your past self has arrived",
                    "html", htmlBody
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, Map.class
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
                      <p style="margin-top:8px; color:#9ca3af; font-size:12px;">
                        Or copy this link: <a href="%s" style="color:#4f46e5;">%s</a>
                      </p>
                    </div>
                    """.formatted(capsule.getVideoUrl(), capsule.getVideoUrl(), capsule.getVideoUrl());
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
                      <p style="color:#166534; font-size:15px; line-height:1.7; margin-bottom:16px;">%s</p>
                    </div>
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
                <body style="margin:0; padding:0; background:#f9fafb; font-family:-apple-system,
                             BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background:#f9fafb; padding:40px 20px;">
                    <tr>
                      <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0"
                               style="background:#ffffff; border-radius:16px;
                                      box-shadow:0 4px 24px rgba(0,0,0,0.07); overflow:hidden;
                                      max-width:600px; width:100%%;">

                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg,#4f46e5 0%%,#7c3aed 100%%);
                                       padding:40px 40px 32px; text-align:center;">
                              <p style="color:rgba(255,255,255,0.75); font-size:13px;
                                        letter-spacing:0.1em; text-transform:uppercase;
                                        margin:0 0 8px;">Dear Future Me</p>
                              <h1 style="color:#ffffff; font-size:28px; font-weight:700;
                                         margin:0 0 12px; line-height:1.2;">
                                A message from your past self
                              </h1>
                              <p style="color:rgba(255,255,255,0.8); font-size:14px; margin:0;">
                                Written on %s
                              </p>
                            </td>
                          </tr>

                          <!-- Body -->
                          <tr>
                            <td style="padding:36px 40px;">

                              <!-- Note -->
                              <div style="margin-bottom:28px;">
                                <h3 style="color:#6b7280; font-size:13px; font-weight:600;
                                           letter-spacing:0.08em; text-transform:uppercase;
                                           margin:0 0 12px;">
                                  Your message
                                </h3>
                                <div style="background:#f8fafc; border:1px solid #e2e8f0;
                                            border-radius:10px; padding:20px 24px;">
                                  <p style="color:#1e293b; font-size:16px; line-height:1.75;
                                            margin:0; white-space:pre-wrap;">%s</p>
                                </div>
                              </div>

                              %s

                              %s

                              <!-- Footer note -->
                              <p style="color:#9ca3af; font-size:13px; text-align:center;
                                        margin-top:32px; line-height:1.6;">
                                This time capsule was sealed on %s and delivered today
                                just for you.
                              </p>

                            </td>
                          </tr>

                          <!-- Footer -->
                          <tr>
                            <td style="background:#f8fafc; padding:20px 40px;
                                       border-top:1px solid #e2e8f0; text-align:center;">
                              <p style="color:#9ca3af; font-size:12px; margin:0;">
                                Dear Future Me &mdash; Your digital time capsule
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                createdFormatted,
                capsule.getNote(),
                videoSection,
                aiSection,
                createdFormatted
        );
    }
}
