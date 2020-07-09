// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Type holding error details of long-running-operation (LRO) or polling-operation of an LRO.
 */
class Error {
    @JsonProperty(value = "error")
    private String message;
    @JsonProperty(value = "responseStatusCode")
    private int responseStatusCode;
    @JsonProperty(value = "responseBody")
    private String responseBody;

    Error() {
    }

    /**
     * Creates Error.
     *
     * @param message the error message
     * @param responseStatusCode the http status code associated with the error
     * @param responseBody the http response body associated with the error
     */
    Error(String message, int responseStatusCode, String responseBody) {
        this.message = Objects.requireNonNull(message, "'message' cannot be null.");
        this.responseStatusCode = responseStatusCode;
        this.responseBody = responseBody;
    }

    String getMessage() {
        return this.message;
    }

    int getResponseStatusCode() {
        return this.responseStatusCode;
    }

    String getResponseBody() {
        return this.responseBody;
    }
}
