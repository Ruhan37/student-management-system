package com.project.auth.controller;

import com.project.auth.dto.ApiResponse;
import com.project.auth.dto.StudentDTO;
import com.project.auth.dto.StudentUpdateDTO;
import com.project.auth.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ====================================================================
 * STUDENT CONTROLLER (REST API) - Handles student-related endpoints
 * ====================================================================
 *
 * ROLE-BASED ACCESS:
 * - GET endpoints: Both STUDENT and TEACHER
 * - PUT (update): STUDENT (own profile) or TEACHER
 * - DELETE: TEACHER only
 *
 * @PreAuthorize: Method-level security
 * - "hasRole('TEACHER')": Only ROLE_TEACHER can access
 * - "hasAnyRole('STUDENT', 'TEACHER')": Either role can access
 * - "isAuthenticated()": Any logged-in user
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * Get all students
     * Teachers can view all students for management
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getAllStudents() {
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
    }

    /**
     * Get student by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentById(@PathVariable Long id) {
        StudentDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully", student));
    }

    /**
     * Get current student's profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDTO>> getCurrentStudentProfile() {
        StudentDTO student = studentService.getCurrentStudentProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", student));
    }

    /**
     * Update student profile
     * Students can update their own profile
     * Teachers can update any student's profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateDTO updateDTO) {
        StudentDTO student = studentService.updateStudent(id, updateDTO);
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", student));
    }

    /**
     * Update current student's profile
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDTO>> updateCurrentStudentProfile(
            @Valid @RequestBody StudentUpdateDTO updateDTO) {
        StudentDTO student = studentService.updateCurrentStudentProfile(updateDTO);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", student));
    }

    /**
     * Delete student (Teachers only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully"));
    }

    /**
     * Enroll student in course
     */
    @PostMapping("/{studentId}/courses/{courseId}/enroll")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentDTO>> enrollInCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        StudentDTO student = studentService.enrollInCourse(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success("Enrolled in course successfully", student));
    }

    /**
     * Drop a course
     */
    @PostMapping("/{studentId}/courses/{courseId}/drop")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentDTO>> dropCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        StudentDTO student = studentService.dropCourse(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success("Dropped course successfully", student));
    }

    /**
     * Search students by name
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentDTO>>> searchStudents(
            @RequestParam String name) {
        List<StudentDTO> students = studentService.searchStudents(name);
        return ResponseEntity.ok(ApiResponse.success("Search results", students));
    }

    /**
     * Get students by department
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getStudentsByDepartment(
            @PathVariable Long departmentId) {
        List<StudentDTO> students = studentService.getStudentsByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
    }
}
