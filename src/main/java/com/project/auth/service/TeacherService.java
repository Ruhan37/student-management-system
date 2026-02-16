package com.project.auth.service;

import com.project.auth.dto.TeacherDTO;
import com.project.auth.dto.TeacherUpdateDTO;
import com.project.auth.dto.CourseDTO;
import com.project.auth.entity.Course;
import com.project.auth.entity.Department;
import com.project.auth.entity.Teacher;
import com.project.auth.entity.User;
import com.project.auth.exception.AccessDeniedException;
import com.project.auth.exception.ResourceNotFoundException;
import com.project.auth.repository.DepartmentRepository;
import com.project.auth.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ====================================================================
 * TEACHER SERVICE - Business logic for teacher operations
 * ====================================================================
 */
@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Get all teachers
     */
    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAllWithDepartment().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get teacher by ID
     */
    public TeacherDTO getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findByIdWithCourses(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        return convertToDTO(teacher);
    }

    /**
     * Get current logged-in teacher's profile
     */
    public TeacherDTO getCurrentTeacherProfile() {
        User currentUser = getCurrentUser();
        Teacher teacher = teacherRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));
        return convertToDTO(teacher);
    }

    /**
     * Update teacher profile
     */
    @Transactional
    public TeacherDTO updateTeacher(Long id, TeacherUpdateDTO updateDTO) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        // Teachers can only update their own profile
        User currentUser = getCurrentUser();
        if (!teacher.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        // Update fields
        teacher.setName(updateDTO.getName());
        teacher.setPhone(updateDTO.getPhone());
        teacher.setSpecialization(updateDTO.getSpecialization());
        teacher.setQualification(updateDTO.getQualification());

        if (updateDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(updateDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", updateDTO.getDepartmentId()));
            teacher.setDepartment(department);
        }

        if (updateDTO.getSettings() != null) {
            teacher.setSettings(updateDTO.getSettings());
        }

        Teacher updated = teacherRepository.save(teacher);
        return convertToDTO(updated);
    }

    /**
     * Update current teacher's profile
     */
    @Transactional
    public TeacherDTO updateCurrentTeacherProfile(TeacherUpdateDTO updateDTO) {
        User currentUser = getCurrentUser();
        Teacher teacher = teacherRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));

        // Update fields
        teacher.setName(updateDTO.getName());
        teacher.setPhone(updateDTO.getPhone());
        teacher.setSpecialization(updateDTO.getSpecialization());
        teacher.setQualification(updateDTO.getQualification());

        if (updateDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(updateDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", updateDTO.getDepartmentId()));
            teacher.setDepartment(department);
        }

        if (updateDTO.getSettings() != null) {
            teacher.setSettings(updateDTO.getSettings());
        }

        Teacher updated = teacherRepository.save(teacher);
        return convertToDTO(updated);
    }

    /**
     * Get teachers by department
     */
    public List<TeacherDTO> getTeachersByDepartment(Long departmentId) {
        return teacherRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search teachers by name
     */
    public List<TeacherDTO> searchTeachers(String name) {
        return teacherRepository.searchByName(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private TeacherDTO convertToDTO(Teacher teacher) {
        List<CourseDTO> courseDTOs = null;
        if (teacher.getCourses() != null && !teacher.getCourses().isEmpty()) {
            courseDTOs = teacher.getCourses().stream()
                    .map(this::convertCourseToDTO)
                    .collect(Collectors.toList());
        }

        return TeacherDTO.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .email(teacher.getEmail())
                .teacherId(teacher.getTeacherId())
                .phone(teacher.getPhone())
                .specialization(teacher.getSpecialization())
                .qualification(teacher.getQualification())
                .joinDate(teacher.getJoinDate())
                .settings(teacher.getSettings())
                .departmentId(teacher.getDepartment() != null ? teacher.getDepartment().getId() : null)
                .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                .courses(courseDTOs)
                .courseCount(teacher.getCourses() != null ? teacher.getCourses().size() : 0)
                .build();
    }

    private CourseDTO convertCourseToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .credits(course.getCredits())
                .build();
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
