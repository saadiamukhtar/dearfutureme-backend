package com.dearfutureme.backend.service;

import com.dearfutureme.backend.dto.CapsuleRequest;
import com.dearfutureme.backend.dto.CapsuleResponse;
import com.dearfutureme.backend.entity.Capsule;
import com.dearfutureme.backend.entity.DeliveryOption;
import com.dearfutureme.backend.repository.CapsuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final ClaudeService claudeService;

    @Value("${app.test-mode}")
    private boolean testMode;

    /**
     * Creates a new time capsule.
     * - Validates test-mode options are only used in test mode
     * - Calculates the delivery date from the chosen option
     * - Calls Claude API to generate an AI reflection summary
     * - Persists and returns the saved capsule
     */
    @Transactional
    public CapsuleResponse createCapsule(CapsuleRequest request) {
        DeliveryOption option = request.getDeliveryOption();

        if (option.isTestOption() && !testMode) {
            throw new IllegalArgumentException(
                    "Test delivery options (" + option.getLabel() + ") are only available in test mode. " +
                    "Please choose a production delivery option: ONE_MONTH, THREE_MONTHS, SIX_MONTHS, or ONE_YEAR."
            );
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deliveryDate = option.calculateDeliveryDate(now);

        Capsule capsule = new Capsule();
        capsule.setVideoUrl(request.getVideoUrl());
        capsule.setNote(request.getNote());
        capsule.setEmail(request.getEmail());
        capsule.setDeliveryOption(option);
        capsule.setDeliveryDate(deliveryDate);
        capsule.setPublic(request.isPublic());

        // Generate AI reflection via Claude
        log.info("Requesting AI reflection for new capsule from {}", request.getEmail());
        String aiSummary = claudeService.generateReflection(request.getNote());
        if (aiSummary != null) {
            capsule.setAiSummary(aiSummary);
            log.info("AI reflection generated successfully");
        } else {
            log.warn("AI reflection could not be generated; proceeding without it");
        }

        Capsule saved = capsuleRepository.save(capsule);
        log.info("Capsule created: id={}, email={}, deliveryDate={}", saved.getId(), saved.getEmail(), deliveryDate);

        return new CapsuleResponse(saved);
    }

    /**
     * Returns all public capsules (community wall), newest first.
     */
    @Transactional(readOnly = true)
    public List<CapsuleResponse> getPublicCapsules() {
        return capsuleRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(CapsuleResponse::new)
                .toList();
    }

    /**
     * Returns all capsules belonging to the given email, newest first.
     */
    @Transactional(readOnly = true)
    public List<CapsuleResponse> getCapsulesByEmail(String email) {
        return capsuleRepository.findByEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(CapsuleResponse::new)
                .toList();
    }
}
