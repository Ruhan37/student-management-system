package com.project.auth.security;

import com.project.auth.entity.User;
import com.project.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ====================================================================
 * CUSTOM USER DETAILS SERVICE
 * ====================================================================
 * 
 * This service loads user data for Spring Security.
 * 
 * WHY CUSTOM IMPLEMENTATION?
 * - Spring Security doesn't know how we store users
 * - We need to tell it how to find users by username (email)
 * - We return our User entity which implements UserDetails
 * 
 * WHEN IS THIS CALLED?
 * 1. During login (AuthenticationManager.authenticate())
 * 2. During JWT validation (to load full user details)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by username (email in our case)
     * 
     * @param username The email to search for
     * @return UserDetails (our User entity implements this)
     * @throws UsernameNotFoundException if user not found
     * 
     * @Transactional: Ensures database session stays open
     * (needed if User has lazy-loaded fields)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user by email
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + username
                ));

        // Our User entity already implements UserDetails, so we can return it directly
        return user;
    }

    /**
     * Load user by ID (useful for some operations)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + id
                ));
        return user;
    }
}
