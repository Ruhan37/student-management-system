package com.project.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ====================================================================
 * LOGIN REQUEST DTO - Data sent when user logs in
 * ====================================================================
 *
 * WHY USE DTOs?
 * 1. Security: Don't expose entity structure to clients
 * 2. Validation: Apply validation rules specific to the use case
 * 3. Flexibility: Send/receive different data than what's stored
 * 4. Decoupling: Changes to entities don't break API contracts
 *
 * VALIDATION ANNOTATIONS:
 * - @NotBlank: Must not be null AND must contain at least one non-whitespace character
 * - @Email: Must be valid email format
 * - @Size: Length constraints
 *
 * COMMON MISTAKE:
 * - @NotNull allows empty strings ""
 * - @NotEmpty allows whitespace-only strings "   "
 * - @NotBlank is usually what you want for strings!
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
