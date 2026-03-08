package com.dearfutureme.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "capsules")
@Getter
@Setter
@NoArgsConstructor
public class Capsule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String videoUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryOption deliveryOption;

    @Column(nullable = false)
    private LocalDateTime deliveryDate;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CapsuleStatus status = CapsuleStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = CapsuleStatus.PENDING;
        }
    }
}
