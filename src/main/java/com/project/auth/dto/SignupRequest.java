package com.project.auth.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ====================================================================
 * SIGNUP REQUEST DTO - Data sent when student registers
 * ====================================================================
 *
 * PASSWORD VALIDATION RULES:
 * - Minimum 6 characters (for demo; use 8+ in production)
 * - You can add pattern validation for complexity:
 *   @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")
 *   This requires at least: 1 digit, 1 lowercase, 1 uppercase, 8 chars
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    /**
     * Department ID - student must select a department
     */
    @NotNull(message = "Department is required")
    private Long departmentId;

    /**
     * Phone number (optional)
     */
    private String phone;
}
