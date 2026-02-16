package com.project.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * ====================================================================
 * DEPARTMENT DTO - Data representation of Department for API responses
 * ====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {

    private Long id;
    private String name;
    private String code;
    private String description;

    // Counts (not full lists to avoid large payloads)
    private Integer studentCount;
    private Integer teacherCount;

    // Optional: Include if specifically requested
    private List<StudentDTO> students;
    private List<TeacherDTO> teachers;
}
