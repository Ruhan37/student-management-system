package com.project.auth.service;

import com.project.auth.dto.AuthResponse;
import com.project.auth.dto.LoginRequest;
import com.project.auth.dto.SignupRequest;
import com.project.auth.entity.*;
import com.project.auth.exception.BadRequestException;
import com.project.auth.exception.DuplicateResourceException;
import com.project.auth.repository.DepartmentRepository;
import com.project.auth.repository.StudentRepository;
import com.project.auth.repository.UserRepository;
import com.project.auth.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ====================================================================
 * AUTHENTICATION SERVICE - Handles login and registration
 * ====================================================================
 *
 * RESPONSIBILITIES:
 * 1. User registration (signup)
 * 2. User login (authentication)
 * 3. JWT token generation
 *
 * TRANSACTION MANAGEMENT:
 * @Transactional ensures database operations are atomic
 * If any step fails, entire transaction is rolled back
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new student
     *
     * STEPS:
     * 1. Validate passwords match
     * 2. Check if email already exists
     * 3. Create User entity with encrypted password
     * 4. Create Student entity linked to User
     * 5. Save both entities
     * 6. Generate and return JWT token
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Step 1: Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Step 2: Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Step 3: Find department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BadRequestException("Invalid department selected"));

        // Step 4: Create User entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // BCrypt encryption!
                .role(Role.ROLE_STUDENT)  // Students self-register
                .build();

        // Step 5: Create Student entity
        Student student = Student.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(department)
                .user(user)
                .studentId(generateStudentId())
                .build();

        // Step 6: Save student (cascades to user)
        studentRepository.save(student);

        // Step 7: Authenticate and generate token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication);

        // Step 8: Return response
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(student.getName())
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .build();
    }

    /**
     * Login existing user
     *
     * STEPS:
     * 1. Authenticate credentials (throws BadCredentialsException if invalid)
     * 2. Generate JWT token
     * 3. Get user name (from Student or Teacher)
     * 4. Return response
     */
    public AuthResponse login(LoginRequest request) {
        // Step 1: Authenticate
        // This throws BadCredentialsException if invalid
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Step 2: Generate token
        String token = jwtTokenProvider.generateToken(authentication);

        // Step 3: Get user details
        User user = (User) authentication.getPrincipal();
        String name = getUserName(user);

        // Step 4: Return response
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(name)
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .build();
    }

    /**
     * Get user's display name (from Student or Teacher entity)
     */
    private String getUserName(User user) {
        if (user.getRole() == Role.ROLE_STUDENT) {
            return studentRepository.findByEmail(user.getEmail())
                    .map(Student::getName)
                    .orElse(user.getEmail());
        } else {
            // For teachers, we'll add TeacherRepository later
            return user.getEmail();
        }
    }

    /**
     * Generate unique student ID
     * Format: STU + year + sequential number
     * Example: STU2024001
     */
    private String generateStudentId() {
        long count = studentRepository.count() + 1;
        int year = java.time.Year.now().getValue();
        return String.format("STU%d%03d", year, count);
    }
}
