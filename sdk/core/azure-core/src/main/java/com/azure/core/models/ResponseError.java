// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * This class represents the error details of an HTTP response.
 */
public final class ResponseError {

    @JsonProperty(value = "code", required = true)
    private final String code;

    @JsonProperty(value = "message", required = true)
    private final String message;

    @JsonProperty(value = "target")
    private String target;

    @JsonProperty(value = "innererror")
    private ResponseInnerError innerError;

    @JsonProperty(value = "details")
    private List<ResponseError> errorDetails;

    /**
     * Creates an instance of {@link ResponseError}.
     *
     * @param code the error code of this error.
     * @param message the error message of this error.
     */
    @JsonCreator
    public ResponseError(@JsonProperty(value = "code", required = true)String code,
                         @JsonProperty(value = "message", required = true)String message) {
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
    String getTarget() {
        return target;
    }

    /**
     * Sets the target of this error.
     *
     * @param target the target of this error.
     * @return the updated {@link ResponseError} instance.
     */
    ResponseError setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Returns the inner error information for this error.
     *
     * @return the inner error for this error.
     */
    ResponseInnerError getInnerError() {
        return innerError;
    }

    /**
     * Sets the inner error information for this error.
     * @param innerError the inner error for this error.
     * @return the updated {@link ResponseError} instance.
     */
    ResponseError setInnerError(ResponseInnerError innerError) {
        this.innerError = innerError;
        return this;
    }

    /**
     * Returns a list of details about specific errors that led to this reported error.
     *
     * @return the error details.
     */
    List<ResponseError> getErrorDetails() {
        return errorDetails;
    }

    /**
     * Sets a list of details about specific errors that led to this reported error.
     *
     * @param errorDetails the error details.
     * @return the updated {@link ResponseError} instance.
     */
    ResponseError setErrorDetails(List<ResponseError> errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }
}
