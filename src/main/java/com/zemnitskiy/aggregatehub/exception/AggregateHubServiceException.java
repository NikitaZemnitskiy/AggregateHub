package com.zemnitskiy.aggregatehub.exception;

/**
 * Custom exception for errors occurring in UserService.
 */
public class AggregateHubServiceException extends RuntimeException {
    public AggregateHubServiceException(String message) {
        super(message);
    }

    public AggregateHubServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}