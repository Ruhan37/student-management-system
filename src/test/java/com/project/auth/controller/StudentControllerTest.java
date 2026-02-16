package com.project.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.auth.dto.StudentDTO;
import com.project.auth.dto.StudentUpdateDTO;
import com.project.auth.entity.Role;
import com.project.auth.entity.User;
import com.project.auth.exception.ResourceNotFoundException;
import com.project.auth.security.CustomUserDetailsService;
import com.project.auth.security.JwtTokenProvider;
import com.project.auth.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ====================================================================
 * STUDENT CONTROLLER TESTS - Unit tests for student endpoints
 * ====================================================================
 *
 * Test Strategy:
 * - @WebMvcTest: Fast integration tests for web layer
 * - @WithMockUser: Simulates authenticated users with specific roles
 * - Tests role-based access control (RBAC)
 * - Validates request/response structure
 */
@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private StudentDTO sampleStudent;
    private List<StudentDTO> studentList;
    private StudentUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        // Setup sample student DTO
        sampleStudent = StudentDTO.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .studentId("STU2024001")
                .phone("+1234567890")
                .address("123 Main St")
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .enrollmentDate(LocalDate.of(2024, 1, 1))
                .departmentId(1L)
                .departmentName("Computer Science")
                .enrolledCourseCount(3)
                .courses(Collections.emptyList())
                .build();

        // Setup student list
        StudentDTO student2 = StudentDTO.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .studentId("STU2024002")
                .departmentId(2L)
                .departmentName("Mathematics")
                .build();

        studentList = Arrays.asList(sampleStudent, student2);

        // Setup update DTO
        updateDTO = StudentUpdateDTO.builder()
                .name("John Updated")
                .phone("+0987654321")
                .address("456 Oak Ave")
                .build();
    }

    // ==================== GET ALL STUDENTS ====================

    @Nested
    @DisplayName("GET /api/students")
    class GetAllStudentsTests {

        @Test
        @DisplayName("Teacher should get all students")
        @WithMockUser(roles = "TEACHER")
        void getAllStudents_asTeacher_returnsStudentList() throws Exception {
            // Arrange
            when(studentService.getAllStudents()).thenReturn(studentList);

            // Act & Assert
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].name").value("John Doe"))
                    .andExpect(jsonPath("$.data[1].name").value("Jane Smith"));

            verify(studentService, times(1)).getAllStudents();
        }

        @Test
        @DisplayName("Student should be forbidden from getting all students")
        @WithMockUser(roles = "STUDENT")
        void getAllStudents_asStudent_returnsForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isForbidden());

            verify(studentService, never()).getAllStudents();
        }

        @Test
        @DisplayName("Unauthenticated user should be unauthorized")
        void getAllStudents_unauthenticated_returnsUnauthorized() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET STUDENT BY ID ====================

    @Nested
    @DisplayName("GET /api/students/{id}")
    class GetStudentByIdTests {

        @Test
        @DisplayName("Teacher should get student by ID")
        @WithMockUser(roles = "TEACHER")
        void getStudentById_asTeacher_returnsStudent() throws Exception {
            // Arrange
            when(studentService.getStudentById(1L)).thenReturn(sampleStudent);

            // Act & Assert
            mockMvc.perform(get("/api/students/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("John Doe"))
                    .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

            verify(studentService, times(1)).getStudentById(1L);
        }

        @Test
        @DisplayName("Student should get student by ID")
        @WithMockUser(roles = "STUDENT")
        void getStudentById_asStudent_returnsStudent() throws Exception {
            // Arrange
            when(studentService.getStudentById(1L)).thenReturn(sampleStudent);

            // Act & Assert
            mockMvc.perform(get("/api/students/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("Should return 404 for non-existent student")
        @WithMockUser(roles = "TEACHER")
        void getStudentById_notFound_returns404() throws Exception {
            // Arrange
            when(studentService.getStudentById(999L))
                    .thenThrow(new ResourceNotFoundException("Student", "id", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/students/999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== GET CURRENT STUDENT PROFILE ====================

    @Nested
    @DisplayName("GET /api/students/me")
    class GetCurrentProfileTests {

        @Test
        @DisplayName("Student should get their own profile")
        @WithMockUser(roles = "STUDENT")
        void getCurrentProfile_asStudent_returnsProfile() throws Exception {
            // Arrange
            when(studentService.getCurrentStudentProfile()).thenReturn(sampleStudent);

            // Act & Assert
            mockMvc.perform(get("/api/students/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("John Doe"));

            verify(studentService, times(1)).getCurrentStudentProfile();
        }

        @Test
        @DisplayName("Teacher should be forbidden from /me endpoint")
        @WithMockUser(roles = "TEACHER")
        void getCurrentProfile_asTeacher_returnsForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/students/me"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== UPDATE STUDENT ====================

    @Nested
    @DisplayName("PUT /api/students/{id}")
    class UpdateStudentTests {

        @Test
        @DisplayName("Teacher should update student profile")
        @WithMockUser(roles = "TEACHER")
        void updateStudent_asTeacher_returnsUpdatedStudent() throws Exception {
            // Arrange
            StudentDTO updatedStudent = StudentDTO.builder()
                    .id(1L)
                    .name("John Updated")
                    .phone("+0987654321")
                    .build();
            when(studentService.updateStudent(eq(1L), any(StudentUpdateDTO.class)))
                    .thenReturn(updatedStudent);

            // Act & Assert
            mockMvc.perform(put("/api/students/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("John Updated"));
        }

        @Test
        @DisplayName("Student should update their own profile")
        @WithMockUser(roles = "STUDENT")
        void updateStudent_asStudent_returnsUpdatedStudent() throws Exception {
            // Arrange
            StudentDTO updatedStudent = StudentDTO.builder()
                    .id(1L)
                    .name("John Updated")
                    .build();
            when(studentService.updateStudent(eq(1L), any(StudentUpdateDTO.class)))
                    .thenReturn(updatedStudent);

            // Act & Assert
            mockMvc.perform(put("/api/students/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk());
        }
    }

    // ==================== DELETE STUDENT ====================

    @Nested
    @DisplayName("DELETE /api/students/{id}")
    class DeleteStudentTests {

        @Test
        @DisplayName("Teacher should delete student")
        @WithMockUser(roles = "TEACHER")
        void deleteStudent_asTeacher_returnsSuccess() throws Exception {
            // Arrange
            doNothing().when(studentService).deleteStudent(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/students/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Student deleted successfully"));

            verify(studentService, times(1)).deleteStudent(1L);
        }

        @Test
        @DisplayName("Student should be forbidden from deleting students")
        @WithMockUser(roles = "STUDENT")
        void deleteStudent_asStudent_returnsForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/students/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(studentService, never()).deleteStudent(any());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent student")
        @WithMockUser(roles = "TEACHER")
        void deleteStudent_notFound_returns404() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Student", "id", 999L))
                    .when(studentService).deleteStudent(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/students/999")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}
