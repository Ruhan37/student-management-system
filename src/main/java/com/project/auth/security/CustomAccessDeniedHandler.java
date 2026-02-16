package com.project.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ====================================================================
 * CUSTOM ACCESS DENIED HANDLER
 * ====================================================================
 *
 * This is called when an AUTHENTICATED user tries to access a resource
 * they DON'T HAVE PERMISSION for.
 *
 * DIFFERENCE FROM AuthenticationEntryPoint:
 * - AuthenticationEntryPoint: User is NOT logged in
 * - AccessDeniedHandler: User IS logged in but lacks permission
 *
 * EXAMPLE:
 * - Student logged in, tries to access /api/admin/delete-user
 * - User IS authenticated, but role is STUDENT, not ADMIN
 * - This handler is triggered → 403 Forbidden
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/api/")) {
            // API call: Return JSON error
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                    "{\"success\": false, \"message\": \"Access Denied: You don't have permission to access this resource\"}"
            );
        } else {
            // Web page: Redirect to error page or dashboard with message
            response.sendRedirect("/access-denied");
        }
    }
}
