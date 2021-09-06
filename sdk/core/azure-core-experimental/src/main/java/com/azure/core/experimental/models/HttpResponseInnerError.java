// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.models;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The inner error of a {@link HttpResponseError}.
 */
final class HttpResponseInnerError {

    @JsonProperty(value = "code")
    private String code;

    @JsonProperty(value = "innererror")
    private HttpResponseInnerError innerError;

    /**
     * Returns the error code of the inner error.
     *
     * @return the error code of this inner error.
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the error code of the inner error.
     *
     * @param code the error code of this inner error.
     * @return the updated {@link HttpResponseInnerError} instance.
     */
    public HttpResponseInnerError setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Returns the nested inner error for this error.
     *
     * @return the nested inner error for this error.
     */
    public HttpResponseInnerError getInnerError() {
        return innerError;
    }

    /**
     * Sets the nested inner error for this error.
     *
     * @param innerError the nested inner error for this error.
     * @return the updated {@link HttpResponseInnerError} instance.
     */
    public HttpResponseInnerError setInnerError(HttpResponseInnerError innerError) {
        this.innerError = innerError;
        return this;
    }
}
