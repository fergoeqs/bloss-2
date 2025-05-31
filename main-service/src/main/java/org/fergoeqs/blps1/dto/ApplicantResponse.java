package org.fergoeqs.blps1.dto;

public record ApplicantResponse(
        Long id,
        String name,
        String contactInfo,
        String email
) {}