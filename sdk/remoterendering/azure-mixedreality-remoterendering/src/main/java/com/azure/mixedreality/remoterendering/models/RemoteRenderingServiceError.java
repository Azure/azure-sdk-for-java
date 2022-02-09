// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/** Represents an error in the service. */
@Immutable
public final class RemoteRenderingServiceError {
    private final String code;
    private final String message;
    private final String target;
    private final RemoteRenderingServiceError innerError;
    private final List<RemoteRenderingServiceError> rootErrors;

    /**
     * Constructs a new RemoteRenderingServiceError object.
     *
     * @param code The error code.
     * @param message The human-readable representation of the error.
     * @param target The target of the particular error (e.g., the name of the property in error).
     * @param innerError An object containing more specific information than the current object about the error.
     * @param rootErrors The list of errors that led to this reported error.
     */
    public RemoteRenderingServiceError(String code,
                                       String message,
                                       String target,
                                       RemoteRenderingServiceError innerError,
                                       List<RemoteRenderingServiceError> rootErrors) {
        this.code = code;
        this.message = message;
        this.target = target;
        this.innerError = innerError;
        this.rootErrors = rootErrors;
    }


    /**
     * Get the code property: Error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: A human-readable representation of the error.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the target property: The target of the particular error (e.g., the name of the property in error).
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the innerError property: An object containing more specific information than the current object about the
     * error.
     *
     * @return the innerError value.
     */
    public RemoteRenderingServiceError getInnerError() {
        return this.innerError;
    }

    /**
     * List of errors that led to this reported error.
     *
     * @return the list of errors.
     */
    public List<RemoteRenderingServiceError> listRootErrors() {
        return this.rootErrors;
    }
}
