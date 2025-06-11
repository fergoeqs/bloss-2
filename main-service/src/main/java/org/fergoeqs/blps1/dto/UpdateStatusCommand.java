package org.fergoeqs.blps1.dto;

import java.io.Serializable;

public record UpdateStatusCommand(
        String issueKey,
        String status
) implements Serializable {}
