package com.project.auth.controller;

import com.project.auth.dto.ApiResponse;
import com.project.auth.dto.CourseCreateDTO;
import com.project.auth.dto.CourseDTO;
import com.project.auth.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ====================================================================
 * COURSE CONTROLLER (REST API) - Handles course-related endpoints
 * ====================================================================
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * Get all active courses
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
    }

    /**
     * Get course by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success("Course retrieved successfully", course));
    }

    /**
     * Create new course (Teachers only)
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<CourseDTO>> createCourse(
            @Valid @RequestBody CourseCreateDTO createDTO) {
        CourseDTO course = courseService.createCourse(createDTO);
        return ResponseEntity.ok(ApiResponse.success("Course created successfully", course));
    }

    /**
     * Update course (Teachers only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<CourseDTO>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseCreateDTO updateDTO) {
        CourseDTO course = courseService.updateCourse(id, updateDTO);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", course));
    }

    /**
     * Delete course (Teachers only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully"));
    }

    /**
     * Get courses by teacher
     */
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getCoursesByTeacher(
            @PathVariable Long teacherId) {
        List<CourseDTO> courses = courseService.getCoursesByTeacher(teacherId);
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
    }

    /**
     * Get current teacher's courses
     */
    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getCurrentTeacherCourses() {
        List<CourseDTO> courses = courseService.getCurrentTeacherCourses();
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
    }

    /**
     * Get available courses for student (not enrolled)
     */
    @GetMapping("/available/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getAvailableCourses(
            @PathVariable Long studentId) {
        List<CourseDTO> courses = courseService.getAvailableCoursesForStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success("Available courses retrieved", courses));
    }

    /**
     * Search courses by name
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseDTO>>> searchCourses(
            @RequestParam String name) {
        List<CourseDTO> courses = courseService.searchCourses(name);
        return ResponseEntity.ok(ApiResponse.success("Search results", courses));
    }
}
