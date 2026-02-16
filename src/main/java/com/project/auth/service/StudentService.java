package com.project.auth.service;

import com.project.auth.dto.StudentDTO;
import com.project.auth.dto.StudentUpdateDTO;
import com.project.auth.dto.CourseDTO;
import com.project.auth.entity.Course;
import com.project.auth.entity.Department;
import com.project.auth.entity.Student;
import com.project.auth.entity.User;
import com.project.auth.exception.AccessDeniedException;
import com.project.auth.exception.ResourceNotFoundException;
import com.project.auth.repository.CourseRepository;
import com.project.auth.repository.DepartmentRepository;
import com.project.auth.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ====================================================================
 * STUDENT SERVICE - Business logic for student operations
 * ====================================================================
 */
@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Get all students
     * Used by: Teachers (to view/manage students)
     */
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAllWithDepartment().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get student by ID
     */
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findByIdWithCourses(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return convertToDTO(student);
    }

    /**
     * Get current logged-in student's profile
     */
    public StudentDTO getCurrentStudentProfile() {
        User currentUser = getCurrentUser();
        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        return convertToDTO(student);
    }

    /**
     * Update student profile
     * Students can only update their own profile
     */
    @Transactional
    public StudentDTO updateStudent(Long id, StudentUpdateDTO updateDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Check if current user is the student or a teacher
        User currentUser = getCurrentUser();
        if (!isTeacher(currentUser) && !student.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        // Update fields
        student.setName(updateDTO.getName());
        student.setPhone(updateDTO.getPhone());
        student.setAddress(updateDTO.getAddress());
        student.setDateOfBirth(updateDTO.getDateOfBirth());

        if (updateDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(updateDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", updateDTO.getDepartmentId()));
            student.setDepartment(department);
        }

        if (updateDTO.getSettings() != null) {
            student.setSettings(updateDTO.getSettings());
        }

        Student updated = studentRepository.save(student);
        return convertToDTO(updated);
    }

    /**
     * Update current student's profile
     */
    @Transactional
    public StudentDTO updateCurrentStudentProfile(StudentUpdateDTO updateDTO) {
        User currentUser = getCurrentUser();
        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return updateStudent(student.getId(), updateDTO);
    }

    /**
     * Delete student (Teachers only)
     */
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Remove student from all courses first
        for (Course course : student.getCourses()) {
            course.getStudents().remove(student);
        }
        student.getCourses().clear();

        studentRepository.delete(student);
    }

    /**
     * Enroll student in a course
     */
    @Transactional
    public StudentDTO enrollInCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check if course is full
        if (course.isFull()) {
            throw new AccessDeniedException("Course is full. Cannot enroll.");
        }

        // Check if already enrolled
        if (student.getCourses().contains(course)) {
            throw new AccessDeniedException("Already enrolled in this course");
        }

        student.enrollInCourse(course);
        Student updated = studentRepository.save(student);
        return convertToDTO(updated);
    }

    /**
     * Drop a course
     */
    @Transactional
    public StudentDTO dropCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findByIdWithCourses(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        student.dropCourse(course);
        Student updated = studentRepository.save(student);
        return convertToDTO(updated);
    }

    /**
     * Search students by name
     */
    public List<StudentDTO> searchStudents(String name) {
        return studentRepository.searchByName(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get students by department
     */
    public List<StudentDTO> getStudentsByDepartment(Long departmentId) {
        return studentRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    /**
     * Convert Student entity to DTO
     */
    private StudentDTO convertToDTO(Student student) {
        List<CourseDTO> courseDTOs = null;
        if (student.getCourses() != null && !student.getCourses().isEmpty()) {
            courseDTOs = student.getCourses().stream()
                    .map(this::convertCourseToDTO)
                    .collect(Collectors.toList());
        }

        return StudentDTO.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .studentId(student.getStudentId())
                .phone(student.getPhone())
                .address(student.getAddress())
                .dateOfBirth(student.getDateOfBirth())
                .enrollmentDate(student.getEnrollmentDate())
                .settings(student.getSettings())
                .departmentId(student.getDepartment() != null ? student.getDepartment().getId() : null)
                .departmentName(student.getDepartment() != null ? student.getDepartment().getName() : null)
                .courses(courseDTOs)
                .enrolledCourseCount(student.getCourses() != null ? student.getCourses().size() : 0)
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

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Check if user is a teacher
     */
    private boolean isTeacher(User user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));
    }
}
