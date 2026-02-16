package com.project.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.auth.dto.AuthResponse;
import com.project.auth.dto.LoginRequest;
import com.project.auth.dto.SignupRequest;
import com.project.auth.exception.BadRequestException;
import com.project.auth.exception.DuplicateResourceException;
import com.project.auth.security.CustomUserDetailsService;
import com.project.auth.security.JwtAuthenticationFilter;
import com.project.auth.security.JwtTokenProvider;
import com.project.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ====================================================================
 * AUTH CONTROLLER TESTS - Unit tests for authentication endpoints
 * ====================================================================
 *
 * Test Strategy:
 * - @WebMvcTest: Only loads web layer, fast and focused
 * - @AutoConfigureMockMvc(addFilters = false): Disable security for testing public endpoints
 * - Mock all dependencies (AuthService, Security components)
 * - Test both success and failure scenarios
 * - Validate request body validation
 *
 * Naming Convention: methodName_scenario_expectedBehavior
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private SignupRequest validSignupRequest;
    private LoginRequest validLoginRequest;
    private AuthResponse successAuthResponse;

    @BeforeEach
    void setUp() {
        // Setup valid signup request
        validSignupRequest = SignupRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .confirmPassword("password123")
                .departmentId(1L)
                .phone("+1234567890")
                .build();

        // Setup valid login request
        validLoginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("password123")
                .build();

        // Setup success auth response
        successAuthResponse = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiJ9.test.token")
                .tokenType("Bearer")
                .email("john.doe@example.com")
                .role("ROLE_STUDENT")
                .name("John Doe")
                .expiresIn(10800000L)
                .build();
    }

    // ==================== SIGNUP TESTS ====================

    @Nested
    @DisplayName("POST /api/auth/signup")
    class SignupTests {

        @Test
        @DisplayName("Should register new student successfully")
        void signup_validRequest_returnsSuccess() throws Exception {
            // Arrange
            when(authService.signup(any(SignupRequest.class))).thenReturn(successAuthResponse);

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validSignupRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Registration successful! Welcome aboard!"))
                    .andExpect(jsonPath("$.data.token").exists())
                    .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"));

            verify(authService, times(1)).signup(any(SignupRequest.class));
        }

        @Test
        @DisplayName("Should fail when email already exists")
        void signup_duplicateEmail_returnsConflict() throws Exception {
            // Arrange
            when(authService.signup(any(SignupRequest.class)))
                    .thenThrow(new DuplicateResourceException("User", "email", "john.doe@example.com"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validSignupRequest)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should fail when passwords don't match")
        void signup_passwordMismatch_returnsBadRequest() throws Exception {
            // Arrange
            when(authService.signup(any(SignupRequest.class)))
                    .thenThrow(new BadRequestException("Passwords do not match"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validSignupRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when email is missing")
        void signup_missingEmail_returnsBadRequest() throws Exception {
            // Arrange
            SignupRequest invalidRequest = SignupRequest.builder()
                    .name("John Doe")
                    .password("password123")
                    .confirmPassword("password123")
                    .departmentId(1L)
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when email format is invalid")
        void signup_invalidEmailFormat_returnsBadRequest() throws Exception {
            // Arrange
            validSignupRequest.setEmail("invalid-email");

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validSignupRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when password is too short")
        void signup_shortPassword_returnsBadRequest() throws Exception {
            // Arrange
            validSignupRequest.setPassword("12345");
            validSignupRequest.setConfirmPassword("12345");

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validSignupRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when name is missing")
        void signup_missingName_returnsBadRequest() throws Exception {
            // Arrange
            SignupRequest invalidRequest = SignupRequest.builder()
                    .email("john@example.com")
                    .password("password123")
                    .confirmPassword("password123")
                    .departmentId(1L)
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_validCredentials_returnsToken() throws Exception {
            // Arrange
            when(authService.login(any(LoginRequest.class))).thenReturn(successAuthResponse);

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").exists())
                    .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should fail with invalid credentials")
        void login_invalidCredentials_returnsUnauthorized() throws Exception {
            // Arrange
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Invalid email or password"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should fail when email is missing")
        void login_missingEmail_returnsBadRequest() throws Exception {
            // Arrange
            LoginRequest invalidRequest = LoginRequest.builder()
                    .password("password123")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when password is missing")
        void login_missingPassword_returnsBadRequest() throws Exception {
            // Arrange
            LoginRequest invalidRequest = LoginRequest.builder()
                    .email("john@example.com")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}
