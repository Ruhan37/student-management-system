package com.project.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ====================================================================
 * TEACHER ENTITY - Represents a teacher/instructor
 * ====================================================================
 *
 * RELATIONSHIPS:
 * - Many Teachers belong to One Department (ManyToOne)
 * - One Teacher teaches Many Courses (OneToMany)
 * - One Teacher has One User account (OneToOne)
 *
 * KEY DIFFERENCE FROM STUDENT:
 * - Teachers CANNOT self-register (pre-created by admin)
 * - Teachers have higher privileges (CRUD on students/courses)
 */
@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Teacher's full name
     */
    @Column(nullable = false)
    private String name;

    /**
     * Teacher's email (same as User email)
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Teacher ID number (e.g., "TCH2024001")
     */
    @Column(unique = true)
    private String teacherId;

    /**
     * Phone number
     */
    private String phone;

    /**
     * Specialization/Subject area
     * Example: "Database Systems", "Machine Learning"
     */
    private String specialization;

    /**
     * Qualification
     * Example: "Ph.D. in Computer Science"
     */
    private String qualification;

    /**
     * Date joined
     */
    @Builder.Default
    private LocalDate joinDate = LocalDate.now();

    /**
     * Profile settings stored as JSON string
     */
    @Column(length = 1000)
    @Builder.Default
    private String settings = "{\"theme\": \"light\", \"notifications\": true}";

    /**
     * Many Teachers → One Department
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * One Teacher → Many Courses
     *
     * mappedBy = "teacher": Course entity owns the relationship
     *
     * CascadeType.PERSIST & MERGE:
     * - New courses are saved when teacher is saved
     * - Updated courses are updated when teacher is updated
     * - But deleting teacher won't delete courses (need to handle manually)
     */
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    /**
     * One Teacher → One User (for authentication)
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // ==================== Helper Methods ====================

    /**
     * Add course to teacher
     */
    public void addCourse(Course course) {
        courses.add(course);
        course.setTeacher(this);
    }

    /**
     * Remove course from teacher
     */
    public void removeCourse(Course course) {
        courses.remove(course);
        course.setTeacher(null);
    }
}
