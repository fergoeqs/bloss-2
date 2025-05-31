package org.fergoeqs.blps1.models.enums;

public enum ApplicationStatus {
    RESUME_REQUIRED,
    COVER_LETTER_REQUIRED,
    PENDING,
    PENDING_WITH_WARNING,
    ACCEPTED,
    REJECTED;

    @Override
    public String toString() {
        return name();
    }
}