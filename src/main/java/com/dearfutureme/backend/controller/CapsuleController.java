package com.dearfutureme.backend.controller;

import com.dearfutureme.backend.dto.CapsuleRequest;
import com.dearfutureme.backend.dto.CapsuleResponse;
import com.dearfutureme.backend.service.CapsuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/capsules")
@RequiredArgsConstructor
@Slf4j
public class CapsuleController {

    private final CapsuleService capsuleService;

    /**
     * POST /api/capsules
     * Create a new time capsule.
     *
     * Request body:
     * {
     *   "videoUrl": "https://...",    @PostMapping
     *     public ResponseEntity<CapsuleResponse> createCapsule(@Valid @RequestBody CapsuleRequest request) {
     *         log.info("POST /api/capsules - creating capsule for {}", request.getEmail());
     *         CapsuleResponse response = capsuleService.createCapsule(request);
     *         return ResponseEntity.status(HttpStatus.CREATED).body(response);
     *     }
     *   "note": "Dear future me...",
     *   "email": "user@example.com",
     *   "deliveryOption": "ONE_MONTH",   // or TWO_MINUTES, FIVE_MINUTES, etc. in test mode
     *   "isPublic": true
     * }
     */
    @PostMapping
    public ResponseEntity<CapsuleResponse> createCapsule(@Valid @RequestBody CapsuleRequest request) {
        log.info("POST /api/capsules - creating capsule for {}", request.getEmail());
        CapsuleResponse response = capsuleService.createCapsule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/capsules/community
     * Returns all public capsules for the community wall, newest first.
     */
    @GetMapping("/community")
    public ResponseEntity<List<CapsuleResponse>> getCommunityWall() {
        log.info("GET /api/capsules/community");
        return ResponseEntity.ok(capsuleService.getPublicCapsules());
    }

    /**
     * GET /api/capsules/my?email=user@example.com
     * Returns all capsules for the given email address, newest first.
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyCapsules(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "email query parameter is required"));
        }
        log.info("GET /api/capsules/my?email={}", email);
        return ResponseEntity.ok(capsuleService.getCapsulesByEmail(email));
    }

    /**
     * GET /api/capsules/options
     * Returns all available delivery options (useful for populating frontend dropdowns).
     */
    @GetMapping("/options")
    public ResponseEntity<List<Map<String, String>>> getDeliveryOptions() {
        var options = List.of(
                Map.of("value", "ONE_MONTH",     "label", "1 Month",           "mode", "production"),
                Map.of("value", "THREE_MONTHS",  "label", "3 Months",          "mode", "production"),
                Map.of("value", "SIX_MONTHS",    "label", "6 Months",          "mode", "production"),
                Map.of("value", "ONE_YEAR",       "label", "1 Year",            "mode", "production"),
                Map.of("value", "TWO_MINUTES",   "label", "2 Minutes (Test)",  "mode", "test"),
                Map.of("value", "FIVE_MINUTES",  "label", "5 Minutes (Test)",  "mode", "test"),
                Map.of("value", "TEN_MINUTES",   "label", "10 Minutes (Test)", "mode", "test")
        );
        return ResponseEntity.ok(options);
    }
}
