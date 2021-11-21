// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/** The Calling Server error. */
@Immutable
public final class CallingServerError {
    /*
     * The error code.
     */
    private final String code;

    /*
     * The error message.
     */
    private final String message;

    /*
     * The error target.
     */
    private final String target;

    /*
     * Further details about specific errors that led to this error.
     */
    private final List<CallingServerError> details;

    /*
     * The inner error if any.
     */
    private final CallingServerError innerError;

    /**
     * Get the code property: The error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the target property: The error target.
     *
     * @return the target value.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Get the details property: Further details about specific errors that led to
     * this error.
     *
     * @return the details value.
     */
    public List<CallingServerError> getDetails() {
        return details;
    }

    /**
     * Get the innerError property: The inner error if any.
     *
     * @return the innerError value.
     */
    public CallingServerError getInnerError() {
        return innerError;
    }

    /**
     * Constructs a new CallingServerError
     * @param message The message of the original error
     * @param code The error code
     * @param target The target of the error
     * @param details Additional details
     * @param innerError The inner error
     */
    public CallingServerError(
        String message,
        String code, String target,
        List<CallingServerError> details,
        CallingServerError innerError) {
        this.message = message;
        this.code = code;
        this.target = target;
        this.details = details;
        this.innerError = innerError;
    }
}
