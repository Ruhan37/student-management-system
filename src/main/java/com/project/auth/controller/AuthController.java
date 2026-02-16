package com.project.auth.controller;

import com.project.auth.dto.ApiResponse;
import com.project.auth.dto.AuthResponse;
import com.project.auth.dto.LoginRequest;
import com.project.auth.dto.SignupRequest;
import com.project.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ====================================================================
 * AUTH CONTROLLER (REST API) - Handles authentication endpoints
 * ====================================================================
 *
 * ENDPOINTS:
 * POST /api/auth/signup - Register new student
 * POST /api/auth/login  - Login and get JWT token
 *
 * @RestController = @Controller + @ResponseBody
 * Returns JSON directly (not view names)
 *
 * @RequestMapping("/api/auth"): All endpoints start with /api/auth
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register new student
     *
     * @Valid: Triggers validation annotations in SignupRequest
     * @RequestBody: Parses JSON body to SignupRequest object
     *
     * REQUEST BODY EXAMPLE:
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "password": "password123",
     *   "confirmPassword": "password123",
     *   "departmentId": 1
     * }
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(
            @Valid @RequestBody SignupRequest request) {

        AuthResponse authResponse = authService.signup(request);

        return ResponseEntity.ok(
                ApiResponse.success("Registration successful! Welcome aboard!", authResponse)
        );
    }

    /**
     * Login existing user
     *
     * REQUEST BODY EXAMPLE:
     * {
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     *
     * RESPONSE EXAMPLE:
     * {
     *   "success": true,
     *   "message": "Login successful!",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiJ9...",
     *     "tokenType": "Bearer",
     *     "email": "john@example.com",
     *     "role": "ROLE_STUDENT",
     *     "name": "John Doe",
     *     "expiresIn": 10800000
     *   }
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful!", authResponse)
        );
    }
}
