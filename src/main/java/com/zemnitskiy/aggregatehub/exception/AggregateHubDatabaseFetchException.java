package com.zemnitskiy.aggregatehub.exception;

/**
 * Custom exception for errors occurring while fetching data from databases.
 */
public class AggregateHubDatabaseFetchException extends RuntimeException {
    public AggregateHubDatabaseFetchException(String message) {
        super(message);
    }

    public AggregateHubDatabaseFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}