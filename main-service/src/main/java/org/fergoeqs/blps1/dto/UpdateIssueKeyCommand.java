package org.fergoeqs.blps1.dto;

import java.io.Serializable;

public record UpdateIssueKeyCommand(
        Long applicationId,
        String issueKey
) implements Serializable {}