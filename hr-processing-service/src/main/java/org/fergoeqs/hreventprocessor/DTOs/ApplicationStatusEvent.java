package org.fergoeqs.hreventprocessor.DTOs;

import java.time.LocalDateTime;

public record ApplicationStatusEvent(
        Long applicationId,
        String candidateEmail,
        String candidateName,
        String vacancyTitle,
        String issueKey,
        String status) {
}