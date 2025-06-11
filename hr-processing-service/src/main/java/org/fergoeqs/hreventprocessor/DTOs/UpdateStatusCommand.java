package org.fergoeqs.hreventprocessor.DTOs;

import java.io.Serializable;

public record UpdateStatusCommand(
        String issueKey,
        String status
) implements Serializable {}
