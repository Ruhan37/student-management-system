package com.project.auth.service;

import com.project.auth.dto.CourseCreateDTO;
import com.project.auth.dto.CourseDTO;
import com.project.auth.entity.Course;
import com.project.auth.entity.Teacher;
import com.project.auth.entity.User;
import com.project.auth.exception.DuplicateResourceException;
import com.project.auth.exception.ResourceNotFoundException;
import com.project.auth.repository.CourseRepository;
import com.project.auth.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ====================================================================
 * COURSE SERVICE - Business logic for course operations
 * ====================================================================
 */
@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    /**
     * Get all courses
     */
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAllActiveWithTeacher().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all courses (including inactive)
     */
    public List<CourseDTO> getAllCoursesIncludingInactive() {
        return courseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get course by ID
     */
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findByIdWithStudents(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return convertToDTO(course);
    }

    /**
     * Create new course (Teachers only)
     * Course is assigned to the current teacher
     */
    @Transactional
    public CourseDTO createCourse(CourseCreateDTO createDTO) {
        // Check if course code already exists
        if (courseRepository.existsByCode(createDTO.getCode())) {
            throw new DuplicateResourceException("Course", "code", createDTO.getCode());
        }

        // Get current teacher
        User currentUser = getCurrentUser();
        Teacher teacher = teacherRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));

        // Create course
        Course course = Course.builder()
                .name(createDTO.getName())
                .code(createDTO.getCode())
                .description(createDTO.getDescription())
                .credits(createDTO.getCredits())
                .maxStudents(createDTO.getMaxStudents())
                .active(createDTO.isActive())
                .teacher(teacher)
                .build();

        Course saved = courseRepository.save(course);
        return convertToDTO(saved);
    }

    /**
     * Update course (Teachers only)
     */
    @Transactional
    public CourseDTO updateCourse(Long id, CourseCreateDTO updateDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Check if new code conflicts with another course
        if (!course.getCode().equals(updateDTO.getCode()) &&
                courseRepository.existsByCode(updateDTO.getCode())) {
            throw new DuplicateResourceException("Course", "code", updateDTO.getCode());
        }

        // Update fields
        course.setName(updateDTO.getName());
        course.setCode(updateDTO.getCode());
        course.setDescription(updateDTO.getDescription());
        course.setCredits(updateDTO.getCredits());
        course.setMaxStudents(updateDTO.getMaxStudents());
        course.setActive(updateDTO.isActive());

        Course updated = courseRepository.save(course);
        return convertToDTO(updated);
    }

    /**
     * Delete course (Teachers only)
     */
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findByIdWithStudents(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Remove all students from course
        course.getStudents().forEach(student -> {
            student.getCourses().remove(course);
        });
        course.getStudents().clear();

        courseRepository.delete(course);
    }

    /**
     * Get courses by teacher
     */
    public List<CourseDTO> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get courses taught by current teacher
     */
    public List<CourseDTO> getCurrentTeacherCourses() {
        User currentUser = getCurrentUser();
        Teacher teacher = teacherRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));

        return courseRepository.findByTeacherId(teacher.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get courses a student is enrolled in
     */
    public List<CourseDTO> getCoursesByStudentId(Long studentId) {
        return courseRepository.findCoursesByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get available courses for a student (not enrolled)
     */
    public List<CourseDTO> getAvailableCoursesForStudent(Long studentId) {
        return courseRepository.findCoursesNotEnrolledByStudent(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search courses by name
     */
    public List<CourseDTO> searchCourses(String name) {
        return courseRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private CourseDTO convertToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .description(course.getDescription())
                .credits(course.getCredits())
                .maxStudents(course.getMaxStudents())
                .active(course.isActive())
                .teacherId(course.getTeacher() != null ? course.getTeacher().getId() : null)
                .teacherName(course.getTeacher() != null ? course.getTeacher().getName() : null)
                .enrolledCount(course.getEnrollmentCount())
                .availableSeats(course.getAvailableSeats())
                .isFull(course.isFull())
                .build();
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
