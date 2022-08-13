// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/** The Calling Server error. */
@Immutable
public final class CallAutomationError {
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
    private final List<CallAutomationError> details;

    /*
     * The inner error if any.
     */
    private final CallAutomationError innerError;

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
    public List<CallAutomationError> getDetails() {
        return details;
    }

    /**
     * Get the innerError property: The inner error if any.
     *
     * @return the innerError value.
     */
    public CallAutomationError getInnerError() {
        return innerError;
    }

    /**
     * Constructs a new CallAutomationError
     * @param message The message of the original error
     * @param code The error code
     * @param target The target of the error
     * @param details Additional details
     * @param innerError The inner error
     */
    CallAutomationError(
        String message,
        String code, String target,
        List<CallAutomationError> details,
        CallAutomationError innerError) {
        this.message = message;
        this.code = code;
        this.target = target;
        this.details = details;
        this.innerError = innerError;
    }
}
