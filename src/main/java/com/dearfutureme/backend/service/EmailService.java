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

        // ── Video section ──────────────────────────────────────────────────────────
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

        // ── AI summary section ─────────────────────────────────────────────────────
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

        // ── Full template ──────────────────────────────────────────────────────────
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>A message from your past self</title>
                  <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;1,400&family=Lora:ital,wght@0,400;0,500;1,400&display=swap" rel="stylesheet">
                </head>
                <body style="margin:0;padding:0;background:#f0ebe0;font-family:'Lora',Georgia,serif;">

                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background:#f0ebe0;padding:52px 20px;">
                    <tr>
                      <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0"
                               style="max-width:600px;width:100%%;">

                          <!-- Postmark header -->
                          <tr>
                            <td style="text-align:center;padding-bottom:32px;">
                              <p style="font-family:'Lora',Georgia,serif;color:#a07850;font-size:11px;
                                        letter-spacing:0.2em;text-transform:uppercase;margin:0 0 10px;">
                                Time Capsule &nbsp;&middot;&nbsp; Delivered
                              </p>
                              <table cellpadding="0" cellspacing="0" style="margin:0 auto;">
                                <tr>
                                  <td style="border:2px solid #c4a07a;border-radius:50%;width:68px;
                                             height:68px;text-align:center;vertical-align:middle;">
                                    <span style="font-size:30px;line-height:1;">&#9993;</span>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                          <!-- Main card -->
                          <tr>
                            <td style="background:#fffdf7;border-radius:3px;
                                       box-shadow:0 4px 40px rgba(60,35,10,0.12),
                                                  0 1px 6px rgba(60,35,10,0.07);
                                       overflow:hidden;">

                              <!-- Gold top stripe -->
                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="height:5px;background:linear-gradient(90deg,
                                             #c4a07a 0%%,#e8c898 50%%,#c4a07a 100%%);"></td>
                                </tr>
                              </table>

                              <!-- Card header -->
                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="padding:44px 56px 0;text-align:center;">
                                    <p style="font-family:'Playfair Display',Georgia,serif;
                                              font-style:italic;color:#c4a07a;font-size:14px;
                                              letter-spacing:0.08em;margin:0 0 14px;">
                                      &mdash; dear future me &mdash;
                                    </p>
                                    <h1 style="font-family:'Playfair Display',Georgia,serif;
                                               font-weight:600;color:#1e130a;font-size:32px;
                                               margin:0 0 10px;line-height:1.2;">
                                      A letter from your past self
                                    </h1>
                                    <p style="font-family:'Lora',Georgia,serif;font-style:italic;
                                              color:#9a7c5a;font-size:14px;margin:0 0 36px;">
                                      Written on
                                      <strong style="font-style:normal;color:#7a5a3a;">%s</strong>
                                    </p>
                                  </td>
                                </tr>
                                <!-- Ornamental divider -->
                                <tr>
                                  <td style="padding:0 56px;">
                                    <table width="100%%" cellpadding="0" cellspacing="0">
                                      <tr>
                                        <td style="border-top:1px solid #e0d0bc;"></td>
                                        <td style="padding:0 16px;white-space:nowrap;color:#d4b07a;
                                                   font-size:18px;vertical-align:middle;">&#10087;</td>
                                        <td style="border-top:1px solid #e0d0bc;"></td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>

                              <!-- Message block -->
                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="padding:32px 56px 12px;">
                                    <p style="font-family:'Lora',Georgia,serif;font-size:11px;
                                              font-weight:500;color:#c4a07a;letter-spacing:0.18em;
                                              text-transform:uppercase;margin:0 0 14px;">
                                      Your message
                                    </p>
                                    <div style="background:#fdf6ec;border-left:3px solid #d4a870;
                                                padding:24px 28px;border-radius:0 4px 4px 0;">
                                      <p style="font-family:'Lora',Georgia,serif;font-style:italic;
                                                color:#2e1e0e;font-size:16px;line-height:1.9;
                                                margin:0;white-space:pre-wrap;">%s</p>
                                    </div>
                                  </td>
                                </tr>
                              </table>

                              %s

                              %s

                              <!-- Ornamental divider -->
                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="padding:24px 56px 0;">
                                    <table width="100%%" cellpadding="0" cellspacing="0">
                                      <tr>
                                        <td style="border-top:1px solid #e0d0bc;"></td>
                                        <td style="padding:0 16px;white-space:nowrap;color:#d4b07a;
                                                   font-size:18px;vertical-align:middle;">&#10022;</td>
                                        <td style="border-top:1px solid #e0d0bc;"></td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>

                              <!-- Closing note -->
                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="padding:24px 56px 36px;text-align:center;">
                                    <p style="font-family:'Lora',Georgia,serif;font-style:italic;
                                              color:#a07850;font-size:13px;line-height:1.8;margin:0;">
                                      This capsule was sealed on <em>%s</em> and<br>
                                      delivered to you today &mdash; just as past-you intended.
                                    </p>
                                  </td>
                                </tr>
                              </table>

                              <!-- Footer -->
                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="background:#f5ede0;padding:20px 56px;
                                             border-top:1px solid #ddd0bc;text-align:center;">
                                    <p style="font-family:'Playfair Display',Georgia,serif;
                                              font-style:italic;color:#c4a07a;font-size:14px;
                                              margin:0;letter-spacing:0.04em;">Dear Future Me</p>
                                    <p style="font-family:'Lora',Georgia,serif;color:#c0a888;
                                              font-size:11px;letter-spacing:0.1em;
                                              text-transform:uppercase;margin:4px 0 0;">
                                      Your digital time capsule
                                    </p>
                                  </td>
                                </tr>
                                <!-- Gold bottom stripe -->
                                <tr>
                                  <td style="height:4px;background:linear-gradient(90deg,
                                             #c4a07a 0%%,#e8c898 50%%,#c4a07a 100%%);"></td>
                                </tr>
                              </table>

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
