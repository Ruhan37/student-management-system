package com.project.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ====================================================================
 * JWT AUTH RESPONSE DTO - Returned after successful login
 * ====================================================================
 *
 * JWT FLOW REMINDER:
 * 1. User logs in with email/password
 * 2. Server validates credentials
 * 3. Server creates JWT token containing user info
 * 4. Server returns this AuthResponse with the token
 * 5. Client stores token and sends it with every request
 *
 * WHAT'S IN THE RESPONSE:
 * - token: The JWT string to use for authentication
 * - tokenType: Usually "Bearer" (standard OAuth2 type)
 * - email: User's email for display purposes
 * - role: User's role (STUDENT/TEACHER) for UI decisions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * The JWT token
     * Client sends this in header: Authorization: Bearer <token>
     */
    private String token;

    /**
     * Token type - almost always "Bearer"
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * User's email
     */
    private String email;

    /**
     * User's role (ROLE_STUDENT or ROLE_TEACHER)
     */
    private String role;

    /**
     * User's display name
     */
    private String name;

    /**
     * Token expiration time in milliseconds
     */
    private Long expiresIn;
}
