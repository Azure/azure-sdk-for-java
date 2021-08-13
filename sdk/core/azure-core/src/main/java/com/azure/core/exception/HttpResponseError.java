// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents the error details of an HTTP response.
 */
public final class HttpResponseError implements Serializable {
    private static final long serialVersionUID = 6945948383457635564L;

    private final String code;
    private final String message;

    private String target;
    private HttpResponseInnerError innerError;
    private List<HttpResponseError> errorDetails;

    /**
     * Creates an instance of {@link HttpResponseError}.
     *
     * @param code the error code of this error.
     * @param message the error message of this error.
     */
    public HttpResponseError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Returns the error code of this error.
     *
     * @return the error code of this error.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the error message of this error.
     *
     * @return the error message of this error.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the target of this error.
     *
     * @return the target of this error.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the target of this error.
     *
     * @param target the target of this error.
     * @return the updated {@link HttpResponseError} instance.
     */
    public HttpResponseError setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Returns the inner error information for this error.
     *
     * @return the inner error for this error.
     */
    public HttpResponseInnerError getInnerError() {
        return innerError;
    }

    /**
     * Sets the inner error information for this error.
     * @param innerError the inner error for this error.
     * @return the updated {@link HttpResponseError} instance.
     */
    public HttpResponseError setInnerError(HttpResponseInnerError innerError) {
        this.innerError = innerError;
        return this;
    }

    /**
     * Returns a list of details about specific errors that led to this reported error.
     *
     * @return the error details.
     */
    public List<HttpResponseError> getErrorDetails() {
        return errorDetails;
    }

    /**
     * Sets a list of details about specific errors that led to this reported error.
     *
     * @param errorDetails the error details.
     * @return the updated {@link HttpResponseError} instance.
     */
    public HttpResponseError setErrorDetails(List<HttpResponseError> errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }
}
