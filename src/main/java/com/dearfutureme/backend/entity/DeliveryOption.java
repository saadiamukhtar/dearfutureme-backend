package com.dearfutureme.backend.entity;

import java.time.LocalDateTime;

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
}
