package com.project.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * ====================================================================
 * STUDENT ENTITY - Represents a student in the system
 * ====================================================================
 *
 * RELATIONSHIPS:
 * - Many Students belong to One Department (ManyToOne)
 * - Many Students enroll in Many Courses (ManyToMany)
 * - One Student has One User account (OneToOne)
 *
 * IMPORTANT - @JoinColumn vs @JoinTable:
 * - @JoinColumn: Creates foreign key in THIS table (ManyToOne, OneToOne)
 * - @JoinTable: Creates a SEPARATE join table (ManyToMany)
 */
@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Student's full name
     */
    @Column(nullable = false)
    private String name;

    /**
     * Student's email (same as User email)
     * Stored here for easy access without joining User table
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Student ID number (e.g., "STU2024001")
     */
    @Column(unique = true)
    private String studentId;

    /**
     * Phone number
     */
    private String phone;

    /**
     * Address
     */
    @Column(length = 500)
    private String address;

    /**
     * Date of birth
     */
    private LocalDate dateOfBirth;

    /**
     * Enrollment date
     */
    @Builder.Default
    private LocalDate enrollmentDate = LocalDate.now();

    /**
     * Profile settings stored as JSON string
     * Example: {"theme": "dark", "notifications": true}
     *
     * In production, consider using @Type for proper JSON handling
     * or create a separate Settings entity
     */
    @Column(length = 1000)
    @Builder.Default
    private String settings = "{\"theme\": \"light\", \"notifications\": true}";

    /**
     * Many Students → One Department
     *
     * @JoinColumn: Creates foreign key "department_id" in students table
     *
     * This is the OWNING side of the relationship
     * (because it has the foreign key)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * Many Students ↔ Many Courses
     *
     * @JoinTable: Creates a separate table "student_courses"
     * - joinColumns: Foreign key to this entity (student_id)
     * - inverseJoinColumns: Foreign key to other entity (course_id)
     *
     * WHY HashSet instead of ArrayList?
     * - Set prevents duplicate enrollments
     * - HashSet is faster for contains() checks
     *
     * CASCADE explained:
     * - We don't want deleting a student to delete courses!
     * - So NO CascadeType.REMOVE here
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @Builder.Default
    private Set<Course> courses = new HashSet<>();

    /**
     * One Student → One User (for authentication)
     *
     * CascadeType.ALL: When we save/delete Student, also save/delete User
     * orphanRemoval: If User is removed from Student, delete the orphaned User
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // ==================== Helper Methods ====================

    /**
     * Enroll student in a course
     */
    public void enrollInCourse(Course course) {
        courses.add(course);
        course.getStudents().add(this);
    }

    /**
     * Drop a course
     */
    public void dropCourse(Course course) {
        courses.remove(course);
        course.getStudents().remove(this);
    }
}
