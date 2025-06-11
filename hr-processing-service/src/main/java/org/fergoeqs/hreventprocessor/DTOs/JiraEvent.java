package org.fergoeqs.hreventprocessor.DTOs;


public record JiraEvent(
        String candidateEmail,
        String candidateName,
        String vacancyTitle,
        String issueKey
) {}
