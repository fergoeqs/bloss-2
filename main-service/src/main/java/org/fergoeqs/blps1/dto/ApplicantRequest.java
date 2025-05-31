package org.fergoeqs.blps1.dto;

public record ApplicantRequest(
        String name,
        String contactInfo,
        String email
) {
}
