package com.zemnitskiy.aggregatehub.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representing error responses.
 */
@Schema(description = "Error response containing message and details")
@Getter
@Setter
public class ErrorResponse {
    @Schema(description = "Error message", example = "Invalid user data provided")
    private String message;

    @Schema(description = "Detailed error information", example = "Username cannot be empty")
    private String details;

    @Schema(description = "Timestamp when the error occurred", example = "2024-12-11T17:45:26.473+01:00")
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, String details) {
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}