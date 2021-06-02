// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

/**
 * The error details of a failed log query.
 */
@Immutable
public final class LogsQueryErrorDetails {
    private final String message;
    private final String code;
    private final String target;

    /**
     * Creates an instance of {@link LogsQueryErrorDetails} with the failure code and target.
     * @param message The error message.
     * @param code The error code indicating the reason for the error.
     * @param target Indicates which property in the request is responsible for the error.
     */
    public LogsQueryErrorDetails(String message, String code, String target) {

        this.message = message;
        this.code = code;
        this.target = target;
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
     * Indicates which property in the request is responsible for the error.
     * @return The property in the request that is responsible for the error.
     */
    public String getTarget() {
        return target;
    }
}
