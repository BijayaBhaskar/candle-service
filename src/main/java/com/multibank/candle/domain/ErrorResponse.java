package com.multibank.candle.domain;

import java.time.Instant;

/**
 * Standard error response for REST APIs.
 * @param timestamp time of error
 * @param status HTTP status code
 * @param error short error description
 * @param message detailed error message
 * @param path request path
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}
