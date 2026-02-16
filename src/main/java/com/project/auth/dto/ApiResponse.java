package com.project.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * ====================================================================
 * API RESPONSE DTO - Standardized API response wrapper
 * ====================================================================
 *
 * WHY WRAP RESPONSES?
 * - Consistent structure for all API responses
 * - Easy to add metadata (timestamp, status, message)
 * - Clients know what to expect
 * - Easier error handling
 *
 * USAGE:
 * return ApiResponse.success("User created", userDTO);
 * return ApiResponse.error("User not found");
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * Success/failure indicator
     */
    private boolean success;

    /**
     * Human-readable message
     */
    private String message;

    /**
     * Actual data payload (can be any type)
     */
    private T data;

    /**
     * Timestamp of response
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ==================== Factory Methods ====================

    /**
     * Create success response with data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Create success response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * Create error response with data (e.g., validation errors)
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}
