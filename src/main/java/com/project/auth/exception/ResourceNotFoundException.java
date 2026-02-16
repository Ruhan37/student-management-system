package com.project.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ====================================================================
 * RESOURCE NOT FOUND EXCEPTION
 * ====================================================================
 * 
 * Thrown when requested resource doesn't exist
 * Example: Student with ID 999 not found
 * 
 * @ResponseStatus: Automatically returns 404 status code
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
