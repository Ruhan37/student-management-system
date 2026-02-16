package com.project.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ====================================================================
 * COURSE DTO - Data representation of Course for API responses
 * ====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer credits;
    private Integer maxStudents;
    private boolean active;

    // Teacher info (flattened)
    private Long teacherId;
    private String teacherName;

    // Computed fields
    private Integer enrolledCount;
    private Integer availableSeats;
    private boolean isFull;
}
