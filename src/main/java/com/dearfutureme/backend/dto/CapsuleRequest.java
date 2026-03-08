package com.dearfutureme.backend.dto;

import com.dearfutureme.backend.entity.DeliveryOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapsuleRequest {

    @NotBlank(message = "Video URL is required")
    private String videoUrl;

    @NotBlank(message = "Note/message is required")
    private String note;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotNull(message = "Delivery option is required")
    private DeliveryOption deliveryOption;

    @JsonProperty("isPublic")
    private boolean isPublic;
}
