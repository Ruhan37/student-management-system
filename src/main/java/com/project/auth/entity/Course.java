package com.project.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * ====================================================================
 * COURSE ENTITY - Represents an academic course
 * ====================================================================
 *
 * RELATIONSHIPS:
 * - Many Courses belong to One Teacher (ManyToOne) - OWNING SIDE
 * - Many Courses have Many Students (ManyToMany) - INVERSE SIDE
 *
 * UNDERSTANDING OWNING vs INVERSE SIDE:
 * - OWNING SIDE: Has @JoinColumn or @JoinTable, controls the relationship
 * - INVERSE SIDE: Has mappedBy, just reads the relationship
 * - Database changes ONLY happen through the OWNING side!
 *
 * COMMON MISTAKE:
 * If you add a Student to Course.students (inverse side) without
 * adding Course to Student.courses (owning side), the relationship
 * WON'T be saved! Always update from the owning side, or both sides.
 */
@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Course name (e.g., "Introduction to Programming")
     */
    @Column(nullable = false)
    private String name;

    /**
     * Course code (e.g., "CS101")
     */
    @Column(nullable = false, unique = true)
    private String code;

    /**
     * Course description
     */
    @Column(length = 1000)
    private String description;

    /**
     * Credit hours (e.g., 3, 4)
     */
    @Builder.Default
    private Integer credits = 3;

    /**
     * Maximum students allowed
     */
    @Builder.Default
    private Integer maxStudents = 30;

    /**
     * Course status
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Many Courses → One Teacher
     *
     * This is the OWNING side (has @JoinColumn)
     * Changes here WILL be persisted to database
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    /**
     * Many Courses ↔ Many Students
     *
     * mappedBy = "courses": Student entity owns this relationship
     * This is the INVERSE side - changes here alone won't persist!
     *
     * REMEMBER:
     * To enroll a student, use student.enrollInCourse(course)
     * NOT course.getStudents().add(student)
     */
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Student> students = new HashSet<>();

    // ==================== Helper Methods ====================

    /**
     * Get current enrollment count
     */
    public int getEnrollmentCount() {
        return students.size();
    }

    /**
     * Check if course is full
     */
    public boolean isFull() {
        return students.size() >= maxStudents;
    }

    /**
     * Check if course has available seats
     */
    public int getAvailableSeats() {
        return maxStudents - students.size();
    }
}
