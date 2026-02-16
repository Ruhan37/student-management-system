package com.project.auth.repository;

import com.project.auth.entity.Teacher;
import com.project.auth.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ====================================================================
 * TEACHER REPOSITORY - Database operations for Teacher entity
 * ====================================================================
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    /**
     * Find teacher by email
     */
    Optional<Teacher> findByEmail(String email);

    /**
     * Find teacher by teacher ID (e.g., "TCH2024001")
     */
    Optional<Teacher> findByTeacherId(String teacherId);

    /**
     * Find all teachers in a department
     */
    List<Teacher> findByDepartment(Department department);

    /**
     * Find all teachers in a department by department ID
     */
    List<Teacher> findByDepartmentId(Long departmentId);

    /**
     * Find teacher by user ID (for getting teacher from logged-in user)
     */
    Optional<Teacher> findByUserId(Long userId);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find teacher with courses loaded
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.courses WHERE t.id = :id")
    Optional<Teacher> findByIdWithCourses(@Param("id") Long id);

    /**
     * Find all teachers with department loaded
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department")
    List<Teacher> findAllWithDepartment();

    /**
     * Search teachers by name
     */
    @Query("SELECT t FROM Teacher t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Teacher> searchByName(@Param("name") String name);

    /**
     * Find teachers by specialization
     */
    List<Teacher> findBySpecializationContainingIgnoreCase(String specialization);
}
