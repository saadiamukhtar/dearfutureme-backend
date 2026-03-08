package com.dearfutureme.backend.dto;

import com.dearfutureme.backend.entity.Capsule;
import com.dearfutureme.backend.entity.CapsuleStatus;
import com.dearfutureme.backend.entity.DeliveryOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CapsuleResponse {

    private final Long id;
    private final String videoUrl;
    private final String note;
    private final String email;
    private final DeliveryOption deliveryOption;
    private final String deliveryOptionLabel;
    private final LocalDateTime deliveryDate;

    @JsonProperty("isPublic")
    private final boolean isPublic;

    private final String aiSummary;
    private final CapsuleStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime sentAt;

    public CapsuleResponse(Capsule capsule) {
        this.id = capsule.getId();
        this.videoUrl = capsule.getVideoUrl();
        this.note = capsule.getNote();
        this.email = capsule.getEmail();
        this.deliveryOption = capsule.getDeliveryOption();
        this.deliveryOptionLabel = capsule.getDeliveryOption().getLabel();
        this.deliveryDate = capsule.getDeliveryDate();
        this.isPublic = capsule.isPublic();
        this.aiSummary = capsule.getAiSummary();
        this.status = capsule.getStatus();
        this.createdAt = capsule.getCreatedAt();
        this.sentAt = capsule.getSentAt();
    }
}
