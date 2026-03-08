package com.dearfutureme.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;
import java.util.Map;

public enum DeliveryOption {

    // Production options
    ONE_MONTH {
        @Override
        public LocalDateTime calculateDeliveryDate(LocalDateTime from) {
            return from.plusMonths(1);
        }
        @Override public String getLabel() { return "1 Month"; }
    },
    THREE_MONTHS {
        @Override
        public LocalDateTime calculateDeliveryDate(LocalDateTime from) {
            return from.plusMonths(3);
        }
        @Override public String getLabel() { return "3 Months"; }
    },
    SIX_MONTHS {
        @Override
        public LocalDateTime calculateDeliveryDate(LocalDateTime from) {
            return from.plusMonths(6);
        }
        @Override public String getLabel() { return "6 Months"; }
    },
    ONE_YEAR {
        @Override
        public LocalDateTime calculateDeliveryDate(LocalDateTime from) {
            return from.plusYears(1);
        }
        @Override public String getLabel() { return "1 Year"; }
    },

    // Test mode options
    TWO_MINUTES {
        @Override
        public LocalDateTime calculateDeliveryDate(LocalDateTime from) {
            return from.plusMinutes(2);
        }
        @Override public String getLabel() { return "2 Minutes (Test)"; }
    },
    FIVE_MINUTES {
        @Override
        public LocalDateTime calculateDeliveryDate(LocalDateTime from) {
            return from.plusMinutes(5);
        }
        @Override public String getLabel() { return "5 Minutes (Test)"; }
    },
    TEN_MINUTES {
        @Override
        public LocalDateTime calculateDeliveryDate(LocalDateTime from) {
            return from.plusMinutes(10);
        }
        @Override public String getLabel() { return "10 Minutes (Test)"; }
    };

    public abstract LocalDateTime calculateDeliveryDate(LocalDateTime from);
    public abstract String getLabel();

    public boolean isTestOption() {
        return this == TWO_MINUTES || this == FIVE_MINUTES || this == TEN_MINUTES;
    }

    // Maps every alias the frontend might send → the correct enum constant
    private static final Map<String, DeliveryOption> ALIASES = Map.of(
            "2min",      TWO_MINUTES,
            "5min",      FIVE_MINUTES,
            "10min",     TEN_MINUTES,
            "1month",    ONE_MONTH,
            "3months",   THREE_MONTHS,
            "6months",   SIX_MONTHS,
            "1year",     ONE_YEAR
    );

    @JsonCreator
    public static DeliveryOption fromString(String value) {
        if (value == null) return null;
        // Try exact enum name first (e.g. "TWO_MINUTES")
        try {
            return DeliveryOption.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
        // Fall back to alias map (e.g. "2min")
        DeliveryOption match = ALIASES.get(value.toLowerCase());
        if (match != null) return match;
        throw new IllegalArgumentException(
            "Unknown deliveryOption '" + value + "'. Accepted values: " +
            "ONE_MONTH, THREE_MONTHS, SIX_MONTHS, ONE_YEAR, " +
            "TWO_MINUTES, FIVE_MINUTES, TEN_MINUTES " +
            "(or shorthand: 1month, 3months, 6months, 1year, 2min, 5min, 10min)"
        );
    }
}
