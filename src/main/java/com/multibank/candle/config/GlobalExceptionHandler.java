package com.multibank.candle.config;

import com.multibank.candle.domain.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request){
        log.warn("Invalid request : {}", ex.getMessage());
        return new ErrorResponse(Instant.now(),
                HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handle HandlerMethodValidationException
     */

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(HandlerMethodValidationException ex, HttpServletRequest request){
        log.warn("Validation error : {}", ex.getMessage());
        return new ErrorResponse(Instant.now(),
                HttpStatus.BAD_REQUEST.value(), "Validation Error",
                ex.getMessage(), request.getRequestURI());
    }

}
