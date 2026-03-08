package com.dearfutureme.backend.service;

import com.dearfutureme.backend.entity.Capsule;
import com.dearfutureme.backend.entity.CapsuleStatus;
import com.dearfutureme.backend.repository.CapsuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final CapsuleRepository capsuleRepository;
    private final EmailService emailService;

    /**
     * Runs every 60 seconds.
     * Finds all PENDING capsules whose deliveryDate has passed,
     * attempts to send the email, and marks them SENT or FAILED.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void processDueCapsules() {
        LocalDateTime now = LocalDateTime.now();
        List<Capsule> dueCapsules = capsuleRepository.findDueCapsules(CapsuleStatus.PENDING, now);

        if (dueCapsules.isEmpty()) {
            log.debug("Scheduler tick: no capsules due at {}", now);
            return;
        }

        log.info("Scheduler found {} capsule(s) due for delivery", dueCapsules.size());

        for (Capsule capsule : dueCapsules) {
            try {
                log.info("Sending capsule id={} to {}", capsule.getId(), capsule.getEmail());
                boolean sent = emailService.sendCapsuleEmail(capsule);

                if (sent) {
                    capsule.setStatus(CapsuleStatus.SENT);
                    capsule.setSentAt(LocalDateTime.now());
                    log.info("Capsule id={} marked SENT", capsule.getId());
                } else {
                    capsule.setStatus(CapsuleStatus.FAILED);
                    log.warn("Capsule id={} marked FAILED (email send returned false)", capsule.getId());
                }
            } catch (Exception e) {
                capsule.setStatus(CapsuleStatus.FAILED);
                log.error("Capsule id={} marked FAILED due to exception: {}", capsule.getId(), e.getMessage());
            }

            capsuleRepository.save(capsule);
        }
    }
}
