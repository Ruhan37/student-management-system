package com.project.auth.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ====================================================================
 * COURSE CREATE/UPDATE DTO - Data for creating/updating courses
 * ====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateDTO {

    @NotBlank(message = "Course name is required")
    @Size(min = 2, max = 200, message = "Course name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "Course code is required")
    @Size(min = 2, max = 20, message = "Course code must be between 2 and 20 characters")
    private String code;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    @Builder.Default
    private Integer credits = 3;

    @Min(value = 1, message = "Max students must be at least 1")
    @Max(value = 500, message = "Max students cannot exceed 500")
    @Builder.Default
    private Integer maxStudents = 30;

    @Builder.Default
    private boolean active = true;
}
