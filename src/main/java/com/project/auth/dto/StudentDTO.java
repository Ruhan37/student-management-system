package com.project.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * ====================================================================
 * STUDENT DTO - Data representation of Student for API responses
 * ====================================================================
 *
 * WHY NOT RETURN ENTITY DIRECTLY?
 * 1. Avoid exposing sensitive data (like User passwords)
 * 2. Avoid circular reference issues (Student → Course → Student → ...)
 * 3. Control exactly what data clients receive
 * 4. Can include computed fields not in entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private Long id;
    private String name;
    private String email;
    private String studentId;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate enrollmentDate;
    private String settings;

    // Department info (flattened, not nested entity)
    private Long departmentId;
    private String departmentName;

    // Course info (just IDs and names, not full entities)
    private List<CourseDTO> courses;

    // Computed field
    private Integer enrolledCourseCount;
}
