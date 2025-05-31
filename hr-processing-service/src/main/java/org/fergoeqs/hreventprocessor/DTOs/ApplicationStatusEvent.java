package org.fergoeqs.hreventprocessor.DTOs;

import java.time.LocalDateTime;

public record ApplicationStatusEvent(
    String candidateEmail,
    String candidateName,
    String vacancyTitle){
}