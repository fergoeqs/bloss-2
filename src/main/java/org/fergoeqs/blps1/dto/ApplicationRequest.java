package org.fergoeqs.blps1.dto;

public record ApplicationRequest(
        Long vacancyId,
        Long applicantId,
        Long resumeId,
        String coverLetter
) {}