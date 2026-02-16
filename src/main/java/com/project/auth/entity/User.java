package com.project.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * ====================================================================
 * USER ENTITY - Core authentication entity
 * ====================================================================
 *
 * WHY IMPLEMENTS UserDetails?
 * - Spring Security requires UserDetails interface for authentication
 * - Provides methods like getUsername(), getPassword(), getAuthorities()
 * - Allows Spring Security to work with our custom User entity
 *
 * BEGINNER TIP:
 * - UserDetails is Spring Security's way of representing a logged-in user
 * - getAuthorities() returns the user's roles/permissions
 * - The boolean methods (isEnabled, etc.) control account status
 */
@Entity
@Table(name = "users")  // "user" is reserved keyword in PostgreSQL!
@Data                    // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor       // Lombok: Generates no-args constructor (required by JPA)
@AllArgsConstructor      // Lombok: Generates all-args constructor
@Builder                 // Lombok: Enables builder pattern for object creation
public class User implements UserDetails {

    /**
     * Primary key with auto-increment
     * GenerationType.IDENTITY: Database generates the ID (PostgreSQL SERIAL)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email serves as username for login
     * - unique: No duplicate emails allowed
     * - nullable=false: Required field
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * BCrypt encrypted password
     * NEVER store plain text passwords!
     *
     * BCrypt automatically:
     * - Adds random salt (prevents rainbow table attacks)
     * - Is slow by design (prevents brute force)
     */
    @Column(nullable = false)
    private String password;

    /**
     * User role (STUDENT or TEACHER)
     *
     * @Enumerated(EnumType.STRING):
     * - Stores "ROLE_STUDENT" as string in database
     * - Alternative: EnumType.ORDINAL stores 0, 1, 2... (NOT recommended - fragile!)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Account status flags
     * All default to true for simplicity
     * In production, you might want email verification (enabled=false until verified)
     */
    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean accountNonExpired = true;

    @Builder.Default
    private boolean accountNonLocked = true;

    @Builder.Default
    private boolean credentialsNonExpired = true;

    // ==================== UserDetails Interface Methods ====================

    /**
     * Returns the authorities (roles) granted to the user
     * Spring Security uses this to check permissions
     *
     * Example: @PreAuthorize("hasRole('TEACHER')") checks if
     * getAuthorities() contains "ROLE_TEACHER"
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert our Role enum to Spring Security's GrantedAuthority
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns the username used for authentication
     * We use email as username
     */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
