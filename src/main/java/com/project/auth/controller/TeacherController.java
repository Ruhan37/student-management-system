package com.project.auth.controller;

import com.project.auth.dto.ApiResponse;
import com.project.auth.dto.TeacherDTO;
import com.project.auth.dto.TeacherUpdateDTO;
import com.project.auth.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ====================================================================
 * TEACHER CONTROLLER (REST API) - Handles teacher-related endpoints
 * ====================================================================
 */
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    /**
     * Get all teachers
     * Both students and teachers can view teacher list
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TeacherDTO>>> getAllTeachers() {
        List<TeacherDTO> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success("Teachers retrieved successfully", teachers));
    }

    /**
     * Get teacher by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TeacherDTO>> getTeacherById(@PathVariable Long id) {
        TeacherDTO teacher = teacherService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher retrieved successfully", teacher));
    }

    /**
     * Get current teacher's profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherDTO>> getCurrentTeacherProfile() {
        TeacherDTO teacher = teacherService.getCurrentTeacherProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", teacher));
    }

    /**
     * Update current teacher's profile
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherDTO>> updateCurrentTeacherProfile(
            @Valid @RequestBody TeacherUpdateDTO updateDTO) {
        TeacherDTO teacher = teacherService.updateCurrentTeacherProfile(updateDTO);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", teacher));
    }

    /**
     * Search teachers by name
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TeacherDTO>>> searchTeachers(
            @RequestParam String name) {
        List<TeacherDTO> teachers = teacherService.searchTeachers(name);
        return ResponseEntity.ok(ApiResponse.success("Search results", teachers));
    }

    /**
     * Get teachers by department
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TeacherDTO>>> getTeachersByDepartment(
            @PathVariable Long departmentId) {
        List<TeacherDTO> teachers = teacherService.getTeachersByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Teachers retrieved successfully", teachers));
    }
}
