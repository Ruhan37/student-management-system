package com.project.auth.controller;

import com.project.auth.dto.ApiResponse;
import com.project.auth.dto.DepartmentDTO;
import com.project.auth.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ====================================================================
 * DEPARTMENT CONTROLLER (REST API) - Handles department-related endpoints
 * ====================================================================
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * Get all departments
     * Public during signup, authenticated otherwise
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
    }

    /**
     * Get department by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentById(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", department));
    }

    /**
     * Search departments by name
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> searchDepartments(
            @RequestParam String name) {
        List<DepartmentDTO> departments = departmentService.searchDepartments(name);
        return ResponseEntity.ok(ApiResponse.success("Search results", departments));
    }
}
