package com.project.auth.repository;

import com.project.auth.entity.Course;
import com.project.auth.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ====================================================================
 * COURSE REPOSITORY - Database operations for Course entity
 * ====================================================================
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Find course by code (e.g., "CS101")
     */
    Optional<Course> findByCode(String code);

    /**
     * Check if course code exists
     */
    boolean existsByCode(String code);

    /**
     * Find all courses by teacher
     */
    List<Course> findByTeacher(Teacher teacher);

    /**
     * Find all courses by teacher ID
     */
    List<Course> findByTeacherId(Long teacherId);

    /**
     * Find all active courses
     */
    List<Course> findByActiveTrue();

    /**
     * Search courses by name (partial match)
     */
    List<Course> findByNameContainingIgnoreCase(String name);

    /**
     * Find course with students loaded
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.students WHERE c.id = :id")
    Optional<Course> findByIdWithStudents(@Param("id") Long id);

    /**
     * Find course with teacher loaded
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher WHERE c.id = :id")
    Optional<Course> findByIdWithTeacher(@Param("id") Long id);

    /**
     * Find all courses with teacher loaded (for listing)
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher WHERE c.active = true")
    List<Course> findAllActiveWithTeacher();

    /**
     * Find courses a student is NOT enrolled in
     * Useful for showing available courses to enroll
     */
    @Query("SELECT c FROM Course c WHERE c.active = true AND c.id NOT IN " +
           "(SELECT c2.id FROM Course c2 JOIN c2.students s WHERE s.id = :studentId)")
    List<Course> findCoursesNotEnrolledByStudent(@Param("studentId") Long studentId);

    /**
     * Find courses a student is enrolled in
     */
    @Query("SELECT c FROM Course c JOIN c.students s WHERE s.id = :studentId")
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);

    /**
     * Count students in a course
     */
    @Query("SELECT COUNT(s) FROM Course c JOIN c.students s WHERE c.id = :courseId")
    Long countStudentsInCourse(@Param("courseId") Long courseId);
}
