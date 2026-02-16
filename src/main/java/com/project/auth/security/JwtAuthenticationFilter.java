package com.project.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ====================================================================
 * JWT AUTHENTICATION FILTER
 * ====================================================================
 *
 * This filter runs ONCE for every request (OncePerRequestFilter)
 *
 * WHAT IT DOES:
 * 1. Checks if request has JWT token (in header or cookie)
 * 2. Validates the token
 * 3. If valid, sets user as authenticated in SecurityContext
 * 4. Request continues to controller
 *
 * WHERE IT FITS IN THE CHAIN:
 * Request → JwtAuthFilter → SecurityConfig checks → Controller
 *
 * TOKEN LOCATION:
 * - API calls: Authorization header → "Bearer <token>"
 * - Web pages: Cookie → "jwt=<token>"
 *
 * COMMON BEGINNER MISTAKE:
 * Not clearing SecurityContext between requests!
 * OncePerRequestFilter handles this automatically.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Step 1: Extract JWT token from request
            String jwt = extractJwtFromRequest(request);

            // Step 2: If token exists and user not already authenticated
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 3: Extract username from token
                String username = jwtTokenProvider.extractUsername(jwt);

                if (username != null) {
                    // Step 4: Load user from database
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Step 5: Validate token
                    if (jwtTokenProvider.validateToken(jwt, userDetails)) {

                        // Step 6: Create authentication token
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,  // No credentials needed (already authenticated via JWT)
                                        userDetails.getAuthorities()
                                );

                        // Add request details (IP, session ID, etc.)
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // Step 7: Set authentication in SecurityContext
                        // This tells Spring Security "this user is authenticated"
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception ex) {
            // Log the error but don't block the request
            // Unauthenticated requests will be handled by security config
            logger.error("Cannot set user authentication: " + ex.getMessage());
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT from request
     *
     * CHECKS TWO PLACES:
     * 1. Authorization header (for API calls)
     * 2. Cookie (for web pages using Thymeleaf)
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // Check Authorization header first (for REST API calls)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Remove "Bearer " prefix
        }

        // Check cookies (for Thymeleaf web pages)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
