package com.project.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * ====================================================================
 * TEACHER DTO - Data representation of Teacher for API responses
 * ====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDTO {

    private Long id;
    private String name;
    private String email;
    private String teacherId;
    private String phone;
    private String specialization;
    private String qualification;
    private LocalDate joinDate;
    private String settings;

    // Department info (flattened)
    private Long departmentId;
    private String departmentName;

    // Courses taught (just basic info)
    private List<CourseDTO> courses;

    // Computed field
    private Integer courseCount;
}
