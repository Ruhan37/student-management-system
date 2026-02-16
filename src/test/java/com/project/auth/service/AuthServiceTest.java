package com.project.auth.service;

import com.project.auth.dto.AuthResponse;
import com.project.auth.dto.LoginRequest;
import com.project.auth.dto.SignupRequest;
import com.project.auth.entity.Department;
import com.project.auth.entity.Role;
import com.project.auth.entity.Student;
import com.project.auth.entity.User;
import com.project.auth.exception.BadRequestException;
import com.project.auth.exception.DuplicateResourceException;
import com.project.auth.repository.DepartmentRepository;
import com.project.auth.repository.StudentRepository;
import com.project.auth.repository.UserRepository;
import com.project.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ====================================================================
 * AUTH SERVICE TESTS - Unit tests for authentication business logic
 * ====================================================================
 *
 * Test Strategy:
 * - @ExtendWith(MockitoExtension.class): Pure unit tests with Mockito
 * - Test business logic, not Spring context
 * - Mock all dependencies
 * - Cover success paths and edge cases
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private SignupRequest validSignupRequest;
    private LoginRequest validLoginRequest;
    private Department testDepartment;
    private User testUser;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        // Setup test department
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("Computer Science");
        testDepartment.setCode("CS");

        // Setup test user
        testUser = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .password("$2a$10$encodedPassword")
                .role(Role.ROLE_STUDENT)
                .build();

        // Setup test student
        testStudent = Student.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .studentId("STU2024001")
                .department(testDepartment)
                .user(testUser)
                .build();

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
    }

    // ==================== SIGNUP TESTS ====================

    @Nested
    @DisplayName("signup()")
    class SignupTests {

        @Test
        @DisplayName("Should register new student successfully")
        void signup_validRequest_returnsAuthResponse() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
            when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtTokenProvider.generateToken(any(Authentication.class)))
                    .thenReturn("test.jwt.token");
            when(jwtTokenProvider.getJwtExpiration()).thenReturn(10800000L);

            // Act
            AuthResponse response = authService.signup(validSignupRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("test.jwt.token");
            assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(response.getRole()).isEqualTo("ROLE_STUDENT");
            assertThat(response.getName()).isEqualTo("John Doe");

            verify(userRepository).existsByEmail("john.doe@example.com");
            verify(departmentRepository).findById(1L);
            verify(passwordEncoder).encode("password123");
            verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw exception when passwords don't match")
        void signup_passwordMismatch_throwsBadRequestException() {
            // Arrange
            validSignupRequest.setConfirmPassword("differentPassword");

            // Act & Assert
            assertThatThrownBy(() -> authService.signup(validSignupRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Passwords do not match");

            verify(userRepository, never()).existsByEmail(anyString());
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void signup_duplicateEmail_throwsDuplicateResourceException() {
            // Arrange
            when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.signup(validSignupRequest))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when department not found")
        void signup_departmentNotFound_throwsBadRequestException() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.signup(validSignupRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Invalid department selected");

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should encode password with BCrypt")
        void signup_validRequest_encodesPassword() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
            when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("token");
            when(jwtTokenProvider.getJwtExpiration()).thenReturn(10800000L);

            // Act
            authService.signup(validSignupRequest);

            // Assert
            verify(passwordEncoder).encode("password123");
        }
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_validCredentials_returnsAuthResponse() {
            // Arrange
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtTokenProvider.generateToken(any(Authentication.class)))
                    .thenReturn("test.jwt.token");
            when(jwtTokenProvider.getJwtExpiration()).thenReturn(10800000L);
            when(studentRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testStudent));

            // Act
            AuthResponse response = authService.login(validLoginRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("test.jwt.token");
            assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(response.getName()).isEqualTo("John Doe");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtTokenProvider).generateToken(authentication);
        }

        @Test
        @DisplayName("Should throw exception with invalid credentials")
        void login_invalidCredentials_throwsBadCredentialsException() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid email or password"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(validLoginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid email or password");

            verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
        }

        @Test
        @DisplayName("Should authenticate with correct email and password")
        void login_validRequest_authenticatesCorrectCredentials() {
            // Arrange
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("token");
            when(jwtTokenProvider.getJwtExpiration()).thenReturn(10800000L);
            when(studentRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testStudent));

            // Act
            authService.login(validLoginRequest);

            // Assert - Verify authentication was called with correct credentials
            verify(authenticationManager).authenticate(
                    argThat(auth -> {
                        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) auth;
                        return token.getPrincipal().equals("john.doe@example.com") &&
                               token.getCredentials().equals("password123");
                    })
            );
        }
    }
}
