// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import com.microsoft.agentserver.api.ApiError;
import com.microsoft.agentserver.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

/**
 * Spring {@link RestControllerAdvice} that converts framework-agnostic
 * {@link ApiException} instances and other common exceptions into the standard
 * error envelope: {@code { "error": { "message", "type", "code",... } }}.
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link ApiException} by mapping to the appropriate HTTP status code
     * and serialising the structured {@link ApiError} body.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, ApiError>> handleApiException(ApiException exception) {
        ApiError error = exception.getError() != null
            ? exception.getError()
            : ApiError.serverError("An internal error occurred.");
        return ResponseEntity.status(exception.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("error", error));
    }

    /**
     * Handles 404 Not Found exceptions.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, ApiError>> handleNotFound(NoHandlerFoundException exception) {
        LOGGER.warn("404 Not Found: {} {}", exception.getHttpMethod(), exception.getRequestURL());
        return ResponseEntity.status(404)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("error", ApiError.invalidRequest("Resource not found")));
    }

    /**
     * Handles all unhandled exceptions as 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, ApiError>> handleGenericException(Exception exception) {
        LOGGER.error("Unhandled exception: {}", exception.getMessage(), exception);
        return ResponseEntity.status(500)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("error", ApiError.serverError("An internal error occurred.")));
    }
}

