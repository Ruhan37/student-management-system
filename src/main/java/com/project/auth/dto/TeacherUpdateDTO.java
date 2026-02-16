package com.project.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ====================================================================
 * TEACHER UPDATE DTO - Data for updating teacher profile
 * ====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherUpdateDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String phone;

    @Size(max = 200, message = "Specialization must be less than 200 characters")
    private String specialization;

    @Size(max = 200, message = "Qualification must be less than 200 characters")
    private String qualification;

    private Long departmentId;

    private String settings;
}
