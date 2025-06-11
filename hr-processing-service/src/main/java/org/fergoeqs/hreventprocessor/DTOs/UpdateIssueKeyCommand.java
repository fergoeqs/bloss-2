package org.fergoeqs.hreventprocessor.DTOs;

import java.io.Serializable;

public record UpdateIssueKeyCommand(
        Long applicationId,
        String issueKey
) implements Serializable {}
