package com.project.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ====================================================================
 * JWT AUTHENTICATION ENTRY POINT
 * ====================================================================
 * 
 * This is called when an UNAUTHENTICATED user tries to access a PROTECTED resource.
 * 
 * WHEN IS THIS TRIGGERED?
 * - No JWT token in request
 * - Invalid/expired JWT token
 * - Trying to access /api/students without being logged in
 * 
 * WHAT WE DO:
 * - For API calls: Return 401 Unauthorized JSON response
 * - For web pages: Redirect to login page
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        
        // Check if it's an API call or web page request
        String requestURI = request.getRequestURI();
        
        if (requestURI.startsWith("/api/")) {
            // API call: Return JSON error
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"success\": false, \"message\": \"Unauthorized: Please login to access this resource\"}"
            );
        } else {
            // Web page: Redirect to login
            response.sendRedirect("/login?error=Please login to continue");
        }
    }
}
