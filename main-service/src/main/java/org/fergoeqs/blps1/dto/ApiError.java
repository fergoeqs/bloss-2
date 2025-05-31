package org.fergoeqs.blps1.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        String message,
        LocalDateTime timestamp,
        List<String> details
) {
    public ApiError(String message) {
        this(message, LocalDateTime.now(), null);
    }
}
