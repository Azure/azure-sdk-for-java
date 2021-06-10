// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The error details of a failed log query.
 */
@Immutable
public final class LogsQueryError {
    private final String message;
    private final String code;
    private final List<LogsQueryErrorDetail> errors;

    /**
     * Creates an instance of {@link LogsQueryError} with the failure code and target.
     * @param message The error message.
     * @param code The error code indicating the reason for the error.
     * @param errors The list of additional error details.
     */
    public LogsQueryError(String message, String code, List<LogsQueryErrorDetail> errors) {
        this.message = message;
        this.code = code;
        this.errors = errors;
    }

    /**
     * Returns the error message.
     * @return The error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the error code indicating the reason for the error
     * @return The error code indicating the reason for the error
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the list of additional error details.
     * @return the list of additional error details.
     */
    public List<LogsQueryErrorDetail> getErrors() {
        return errors;
    }
}
