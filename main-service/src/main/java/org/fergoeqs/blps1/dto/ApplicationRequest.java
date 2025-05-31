package org.fergoeqs.blps1.dto;

import java.io.Serializable;

public record ApplicationRequest(
        Long vacancyId,
        Long applicantId,
        Long resumeId,
        String coverLetter
) implements Serializable {}