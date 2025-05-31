package org.fergoeqs.blps1.dto;


public record VacancyResponse(
        Long id,
        String title,
        String description,
        boolean isResumeRequired,
        boolean isCoverLetterRequired) {}
