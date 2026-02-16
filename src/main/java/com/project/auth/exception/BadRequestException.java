package com.project.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ====================================================================
 * BAD REQUEST EXCEPTION
 * ====================================================================
 *
 * Thrown for invalid requests
 * Example: Passwords don't match, invalid data format
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
