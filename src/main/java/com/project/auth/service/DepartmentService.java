package com.project.auth.service;

import com.project.auth.dto.DepartmentDTO;
import com.project.auth.entity.Department;
import com.project.auth.exception.ResourceNotFoundException;
import com.project.auth.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ====================================================================
 * DEPARTMENT SERVICE - Business logic for department operations
 * ====================================================================
 */
@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Get all departments
     */
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get department by ID
     */
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return convertToDTO(department);
    }

    /**
     * Get department by code
     */
    public DepartmentDTO getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "code", code));
        return convertToDTO(department);
    }

    /**
     * Search departments by name
     */
    public List<DepartmentDTO> searchDepartments(String name) {
        return departmentRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private DepartmentDTO convertToDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .studentCount(department.getStudents() != null ? department.getStudents().size() : 0)
                .teacherCount(department.getTeachers() != null ? department.getTeachers().size() : 0)
                .build();
    }
}
