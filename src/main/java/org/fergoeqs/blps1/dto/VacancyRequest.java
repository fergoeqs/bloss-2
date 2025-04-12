package org.fergoeqs.blps1.dto;

public record VacancyRequest(
         String title,
        String description,
        boolean resumeRequired,
        boolean coverLetterRequired,
        String keywords
) {}