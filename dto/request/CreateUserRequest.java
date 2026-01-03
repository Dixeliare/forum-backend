package com.longdx.forum_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new user
 * 
 * Pattern:
 * - Use records for immutable DTOs (Java 14+)
 * - Add validation annotations
 * - Keep it simple, only fields needed for creation
 */
public record CreateUserRequest(
        @NotBlank(message = "Display name is required")
        @Size(max = 255, message = "Display name must not exceed 255 characters")
        String displayName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password
) {
}

