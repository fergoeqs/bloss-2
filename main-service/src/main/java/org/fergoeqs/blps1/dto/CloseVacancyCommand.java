package org.fergoeqs.blps1.dto;

import java.io.Serializable;

public record CloseVacancyCommand(
        String issueKey
) implements Serializable {}