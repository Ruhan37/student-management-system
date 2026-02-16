package com.project.auth.repository;

import com.project.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ====================================================================
 * USER REPOSITORY - Database operations for User entity
 * ====================================================================
 * 
 * WHY EXTEND JpaRepository?
 * - JpaRepository provides CRUD operations out of the box:
 *   - save(), findById(), findAll(), delete(), count(), etc.
 * - No implementation needed! Spring generates it at runtime.
 * 
 * CUSTOM QUERY METHODS:
 * - Spring Data JPA creates queries from method names automatically!
 * - findByEmail → SELECT * FROM users WHERE email = ?
 * - existsByEmail → SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
 * 
 * NAMING CONVENTION for query methods:
 * - findBy... → returns Optional<Entity> or List<Entity>
 * - existsBy... → returns boolean
 * - countBy... → returns long
 * - deleteBy... → deletes matching records
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (for login)
     * 
     * WHY Optional<User>?
     * - User might not exist → null would cause NullPointerException
     * - Optional forces us to handle the "not found" case explicitly
     * - Better code: user.ifPresent(u -> ...) or user.orElseThrow(...)
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists (for registration validation)
     * Returns true if email is taken, false otherwise
     */
    boolean existsByEmail(String email);
}
