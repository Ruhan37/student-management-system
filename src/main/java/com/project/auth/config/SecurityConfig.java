package com.project.auth.config;

import com.project.auth.security.CustomAccessDeniedHandler;
import com.project.auth.security.CustomUserDetailsService;
import com.project.auth.security.JwtAuthenticationEntryPoint;
import com.project.auth.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ====================================================================
 * SECURITY CONFIGURATION - Heart of Spring Security
 * ====================================================================
 *
 * This class configures:
 * 1. Which URLs are public vs protected
 * 2. Role-based access rules
 * 3. JWT filter integration
 * 4. Password encoding
 * 5. Session management (stateless for JWT)
 *
 * SECURITY FILTER CHAIN ORDER:
 * Request → JwtAuthFilter → SecurityConfig rules → Controller
 *
 * IMPORTANT ANNOTATIONS:
 * @EnableWebSecurity: Enables Spring Security web support
 * @EnableMethodSecurity: Enables @PreAuthorize, @Secured annotations
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Enable @PreAuthorize
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Main security configuration
     *
     * WHAT EACH PART DOES:
     * - csrf().disable(): We use JWT, not sessions, so CSRF not needed
     * - sessionManagement(STATELESS): No server-side sessions
     * - authorizeHttpRequests(): Define access rules
     * - addFilterBefore(): Add JWT filter before standard auth filter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless JWT auth)
            .csrf(csrf -> csrf.disable())

            // Configure exception handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtEntryPoint)  // Handle 401
                .accessDeniedHandler(accessDeniedHandler)  // Handle 403
            )

            // Stateless session (no sessions stored on server)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configure URL access rules
            .authorizeHttpRequests(auth -> auth
                // ===== PUBLIC ENDPOINTS =====
                // Anyone can access these without authentication
                .requestMatchers(
                    "/",                    // Home page
                    "/login",               // Login page
                    "/signup",              // Signup page
                    "/api/auth/**",         // Auth API endpoints
                    "/css/**",              // Static CSS files
                    "/js/**",               // Static JS files
                    "/images/**",           // Static images
                    "/error",               // Error page
                    "/access-denied"        // Access denied page
                ).permitAll()

                // ===== TEACHER ONLY ENDPOINTS =====
                // Only users with ROLE_TEACHER can access
                .requestMatchers(
                    "/api/students/*/delete",   // Delete student
                    "/api/courses/create",      // Create course
                    "/api/courses/*/delete",    // Delete course
                    "/teacher/**"               // Teacher dashboard pages
                ).hasRole("TEACHER")

                // ===== STUDENT ONLY ENDPOINTS =====
                .requestMatchers(
                    "/student/**"           // Student dashboard pages
                ).hasRole("STUDENT")

                // ===== AUTHENTICATED ENDPOINTS =====
                // Any logged-in user (STUDENT or TEACHER)
                .requestMatchers(
                    "/api/courses/**",      // View courses
                    "/api/departments/**",  // View departments
                    "/api/teachers/**",     // View teachers
                    "/dashboard/**",        // Dashboard pages
                    "/profile/**"           // Profile pages
                ).authenticated()

                // ===== ALL OTHER REQUESTS =====
                // Must be authenticated
                .anyRequest().authenticated()
            )

            // Add our JWT filter BEFORE the standard UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // Set authentication provider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * Password Encoder - BCrypt
     *
     * WHY BCRYPT?
     * 1. Automatic salting (random salt per password)
     * 2. Adaptive (can increase work factor as hardware improves)
     * 3. Slow by design (10 rounds ≈ 100ms) - prevents brute force
     *
     * HOW IT WORKS:
     * - Encoding: password → BCrypt hash (different each time due to random salt)
     * - Matching: BCrypt extracts salt from stored hash and re-hashes input
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider
     *
     * Connects UserDetailsService and PasswordEncoder
     * Used during login to validate credentials
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication Manager
     *
     * Used to authenticate users during login
     * Injected into AuthService for login functionality
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
