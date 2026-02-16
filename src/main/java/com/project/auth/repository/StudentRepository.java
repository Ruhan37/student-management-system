package com.project.auth.repository;

import com.project.auth.entity.Student;
import com.project.auth.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ====================================================================
 * STUDENT REPOSITORY - Database operations for Student entity
 * ====================================================================
 *
 * ADVANCED QUERY EXAMPLES:
 * - @Query: Write custom JPQL or native SQL queries
 * - JOIN FETCH: Load related entities in single query (avoids N+1 problem)
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find student by email
     */
    Optional<Student> findByEmail(String email);

    /**
     * Find student by student ID (e.g., "STU2024001")
     */
    Optional<Student> findByStudentId(String studentId);

    /**
     * Find all students in a department
     */
    List<Student> findByDepartment(Department department);

    /**
     * Find all students in a department by department ID
     */
    List<Student> findByDepartmentId(Long departmentId);

    /**
     * Find student by user ID (for getting student from logged-in user)
     */
    Optional<Student> findByUserId(Long userId);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if student ID exists
     */
    boolean existsByStudentId(String studentId);

    /**
     * Search students by name (case-insensitive, partial match)
     *
     * JPQL Query Explanation:
     * - LOWER(s.name): Convert name to lowercase
     * - LIKE %...%: Partial match (contains)
     * - :name: Named parameter
     */
    @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Student> searchByName(@Param("name") String name);

    /**
     * Find student with courses loaded (avoids N+1 problem)
     *
     * N+1 PROBLEM EXPLAINED:
     * - Without JOIN FETCH: 1 query for student + N queries for each course
     * - With JOIN FETCH: 1 query loads everything
     *
     * IMPORTANT: Use this when you KNOW you'll access courses
     */
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.id = :id")
    Optional<Student> findByIdWithCourses(@Param("id") Long id);

    /**
     * Find all students with their department loaded
     */
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.department")
    List<Student> findAllWithDepartment();
}
