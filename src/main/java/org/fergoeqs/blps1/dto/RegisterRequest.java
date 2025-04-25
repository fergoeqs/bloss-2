package org.fergoeqs.blps1.dto;


import jakarta.validation.constraints.*;
import org.fergoeqs.blps1.models.enums.Role;

public record RegisterRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password cannot be blank") String password,
        @NotBlank(message = "Name cannot be blank") String name,
        @NotNull(message = "Role cannot be null") Role role,
        String companyName,  // EMPLOYER_*
        String contactInfo   // EMPLOYER_* и USER
) {}
