package org.fergoeqs.blps1.dto;

public record VacancyRequest(
         String title,
        String description,
        boolean isResumeRequired,
        boolean isCoverLetterRequired,
        String keywords
) {}