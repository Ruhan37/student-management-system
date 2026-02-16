package com.project.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ====================================================================
 * UNAUTHORIZED EXCEPTION
 * ====================================================================
 * 
 * Thrown when authentication fails
 * Example: Invalid credentials, expired token
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
