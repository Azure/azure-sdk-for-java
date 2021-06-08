// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/** The Server Calling Services error. */
@Fluent
public final class ServerCallingError {
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
    private final List<ServerCallingError> details;

    /*
     * The inner error if any.
     */
    private final ServerCallingError innerError;

    /**
     * Get the code property: The error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the target property: The error target.
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the details property: Further details about specific errors that led to
     * this error.
     *
     * @return the details value.
     */
    public List<ServerCallingError> getDetails() {
        return this.details;
    }

    /**
     * Get the innerError property: The inner error if any.
     *
     * @return the innerError value.
     */
    public ServerCallingError getInnerError() {
        return this.innerError;
    }

    /**
     * Constructs a new ServerCallingError
     * @param message The message of the original error
     * @param code The error code
     * @param target The target of the error
     * @param details Additional details
     * @param innerError The inner error
     */
    public ServerCallingError(String message, String code, String target, List<ServerCallingError> details, ServerCallingError innerError) {
        this.message = message;
        this.code = code;
        this.target = target;
        this.details = details;
        this.innerError = innerError;
    }
}
