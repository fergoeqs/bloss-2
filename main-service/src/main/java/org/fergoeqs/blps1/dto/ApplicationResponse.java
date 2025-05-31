package org.fergoeqs.blps1.dto;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        String status,
        String vacancyTitle,
        String applicantName,
        String warningMessage,
        LocalDateTime createdAt,
        String coverLetter,
        Integer remainingSlots,
        String correlationId
) {

    public ApplicationResponse(String correlationId, String message) {
        this(null, null, null, null, message, null, null, null, correlationId);
    }
    public ApplicationResponse(
            Long id,
            String status,
            String vacancyTitle,
            String applicantName,
            String warning,
            LocalDateTime createdAt,
            String coverLetter,
            Integer remainingSlots
    ) {
        this(id, status, vacancyTitle, applicantName, warning, createdAt, coverLetter, remainingSlots, null);
    }
}
