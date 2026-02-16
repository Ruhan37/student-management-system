package com.project.auth.entity;

/**
 * ====================================================================
 * ROLE ENUM - Defines user roles in the system
 * ====================================================================
 *
 * WHY ENUM?
 * - Type safety: Can only use defined values (STUDENT, TEACHER)
 * - No typos: IDE will catch errors like "STUDEN"
 * - Easy to use in @PreAuthorize annotations
 *
 * ROLE NAMING CONVENTION:
 * - Spring Security expects roles to start with "ROLE_" prefix
 * - When checking: hasRole("STUDENT") - Spring adds "ROLE_" automatically
 * - When storing: We store "ROLE_STUDENT" in database
 */
public enum Role {

    /**
     * ROLE_STUDENT:
     * - Can self-register (signup)
     * - Can view/update own profile
     * - Can view courses, teachers, departments
     * - CANNOT modify others' data
     * - CANNOT delete anyone
     */
    ROLE_STUDENT,

    /**
     * ROLE_TEACHER:
     * - Pre-created accounts only (no self-registration)
     * - Can CRUD courses
     * - Can view/update/delete students
     * - Can update own profile
     * - Has higher privileges than STUDENT
     */
    ROLE_TEACHER
}
