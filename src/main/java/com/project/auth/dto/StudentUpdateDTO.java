package com.project.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ====================================================================
 * STUDENT UPDATE DTO - Data for updating student profile
 * ====================================================================
 *
 * Separate from SignupRequest because:
 * - No password fields (password change is separate)
 * - Can't change email (it's the login identifier)
 * - Different validation rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentUpdateDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String phone;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    private LocalDate dateOfBirth;

    private Long departmentId;

    private String settings;
}
