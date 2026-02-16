package com.project.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ====================================================================
 * DEPARTMENT ENTITY - Academic department/faculty
 * ====================================================================
 *
 * RELATIONSHIPS:
 * - One Department has Many Students (OneToMany)
 * - One Department has Many Teachers (OneToMany)
 *
 * IMPORTANT CONCEPT - mappedBy:
 * - "mappedBy" indicates this is the INVERSE side of the relationship
 * - The OWNING side is where the foreign key exists (Student.department)
 * - Always put mappedBy on the "One" side of OneToMany
 *
 * CASCADE TYPES EXPLAINED:
 * - CascadeType.ALL: All operations cascade (PERSIST, MERGE, REMOVE, etc.)
 * - CascadeType.PERSIST: When saving Department, also save new Students
 * - CascadeType.MERGE: When updating Department, also update Students
 * - We use selective cascading to avoid accidental deletions
 */
@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Department name (e.g., "Computer Science", "Mathematics")
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Short code for department (e.g., "CSE", "MATH")
     */
    @Column(unique = true)
    private String code;

    /**
     * Description of the department
     */
    @Column(length = 500)
    private String description;

    /**
     * One Department → Many Students
     *
     * mappedBy = "department":
     * - Student entity has a field called "department"
     * - That field owns the relationship (has the foreign key)
     *
     * FetchType.LAZY:
     * - Students are NOT loaded immediately when Department is fetched
     * - Loaded only when getStudents() is called
     * - Better performance (avoids loading unnecessary data)
     *
     * COMMON MISTAKE: Using FetchType.EAGER loads ALL related data
     * immediately, causing performance issues with large datasets!
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Student> students = new ArrayList<>();

    /**
     * One Department → Many Teachers
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Teacher> teachers = new ArrayList<>();

    // ==================== Helper Methods ====================

    /**
     * Add student to department (maintains bidirectional relationship)
     *
     * WHY HELPER METHODS?
     * - Ensures both sides of relationship are updated
     * - Prevents inconsistent state
     */
    public void addStudent(Student student) {
        students.add(student);
        student.setDepartment(this);
    }

    public void removeStudent(Student student) {
        students.remove(student);
        student.setDepartment(null);
    }

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
        teacher.setDepartment(this);
    }

    public void removeTeacher(Teacher teacher) {
        teachers.remove(teacher);
        teacher.setDepartment(null);
    }
}
