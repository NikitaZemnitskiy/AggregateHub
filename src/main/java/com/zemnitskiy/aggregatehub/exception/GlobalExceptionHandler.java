package com.zemnitskiy.aggregatehub.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle AggregateHubServiceException.
     *
     * @param ex the AggregateHubServiceException
     * @return ResponseEntity with ErrorResponse and appropriate HTTP status
     */
    @ExceptionHandler(AggregateHubServiceException.class)
    public ResponseEntity<ErrorResponse> handleAggregateHubServiceException(AggregateHubServiceException ex) {
        logger.error("AggregateHubServiceException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("AggregateHub Service Error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle AggregateHubDatabaseFetchException.
     *
     * @param ex the AggregateHubDatabaseFetchException
     * @return ResponseEntity with ErrorResponse and appropriate HTTP status
     */
    @ExceptionHandler(AggregateHubDatabaseFetchException.class)
    public ResponseEntity<ErrorResponse> handleAggregateHubDatabaseFetchException(AggregateHubDatabaseFetchException ex) {
        logger.error("AggregateHubDatabaseFetchException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("AggregateHub Database Fetch Error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions.
     *
     * @param ex the Exception
     * @return ResponseEntity with ErrorResponse and appropriate HTTP status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Internal Server Error", "An unexpected error occurred.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}