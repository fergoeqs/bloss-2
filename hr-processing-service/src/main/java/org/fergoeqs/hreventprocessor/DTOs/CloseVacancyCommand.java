package org.fergoeqs.hreventprocessor.DTOs;

import java.io.Serializable;

public record CloseVacancyCommand(
        String issueKey
) implements Serializable {}