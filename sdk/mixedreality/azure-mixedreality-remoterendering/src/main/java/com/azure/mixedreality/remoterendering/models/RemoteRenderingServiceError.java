// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering.models;

import java.util.List;

/** Represents an error in the service. */
public final class RemoteRenderingServiceError {
    private String code;
    private String message;
    private String target;
    private RemoteRenderingServiceError innerError;
    private List<RemoteRenderingServiceError> rootErrors;

    /**
     * Set the code property: Error code.
     *
     * @param code the code value.
     * @return this RemoteRenderingServiceError object.
     */
    public RemoteRenderingServiceError setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Set the message property: A human-readable representation of the error.
     *
     * @param message the message value.
     * @return this RemoteRenderingServiceError object.
     */
    public RemoteRenderingServiceError setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Set the target property: The target of the particular error (e.g., the name of the property in error).
     *
     * @param target the target value.
     * @return this RemoteRenderingServiceError object.
     */
    public RemoteRenderingServiceError setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Set the innerError property: An object containing more specific information than the current object about the
     * error.
     *
     * @param innerError the innerError value.
     * @return this RemoteRenderingServiceError object.
     */
    public RemoteRenderingServiceError setInnerError(RemoteRenderingServiceError innerError) {
        this.innerError = innerError;
        return this;
    }

    /**
     * Set the list of errors that led to this reported error.
     *
     * @param rootErrors the rootErrors value.
     * @return the list of errors.
     */
    public RemoteRenderingServiceError setRootErrors(List<RemoteRenderingServiceError> rootErrors) {
        this.rootErrors = rootErrors;
        return this;
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
