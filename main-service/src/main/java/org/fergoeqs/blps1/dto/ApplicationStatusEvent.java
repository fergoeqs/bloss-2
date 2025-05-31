package org.fergoeqs.blps1.dto;

public record ApplicationStatusEvent(
        String candidateEmail,
        String candidateName,
        String vacancyTitle
) {
}
