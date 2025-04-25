package org.fergoeqs.blps1.dto;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        String status,
        String vacancyTitle,
        String applicantName,
        String warningMessage,
        LocalDateTime createdAt,
        String coverLetter
) {}
