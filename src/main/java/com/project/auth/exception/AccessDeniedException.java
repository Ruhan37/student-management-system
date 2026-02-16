package com.project.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ====================================================================
 * ACCESS DENIED EXCEPTION
 * ====================================================================
 * 
 * Thrown when user doesn't have permission for an action
 * Example: Student trying to delete another student
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
