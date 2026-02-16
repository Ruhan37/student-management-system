package com.project.auth.repository;

import com.project.auth.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ====================================================================
 * DEPARTMENT REPOSITORY - Database operations for Department entity
 * ====================================================================
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Find department by name
     */
    Optional<Department> findByName(String name);

    /**
     * Find department by code (e.g., "CSE")
     */
    Optional<Department> findByCode(String code);

    /**
     * Check if department name exists
     */
    boolean existsByName(String name);

    /**
     * Check if department code exists
     */
    boolean existsByCode(String code);

    /**
     * Search departments by name (partial match)
     */
    List<Department> findByNameContainingIgnoreCase(String name);

    /**
     * Find department with all students loaded
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.students WHERE d.id = :id")
    Optional<Department> findByIdWithStudents(@Param("id") Long id);

    /**
     * Find department with all teachers loaded
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.teachers WHERE d.id = :id")
    Optional<Department> findByIdWithTeachers(@Param("id") Long id);
}
