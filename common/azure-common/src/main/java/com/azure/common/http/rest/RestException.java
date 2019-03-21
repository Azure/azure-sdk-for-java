/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.http.rest;

import com.azure.common.http.HttpResponse;

/**
 * An exception thrown for an invalid response with custom error information.
 */
public class RestException extends RuntimeException {
    /**
     * Information about the associated HTTP response.
     */
    private HttpResponse response;

    /**
     * The HTTP response body.
     */
    private Object body;

    /**
     * Initializes a new instance of the RestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public RestException(String message, HttpResponse response) {
        super(message);
        this.response = response;
    }

    /**
     * Initializes a new instance of the RestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param body the deserialized response body
     */
    public RestException(String message, HttpResponse response, Object body) {
        super(message);
        this.response = response;
        this.body = body;
    }

    /**
     * Initializes a new instance of the RestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this RestException
     */
    public RestException(String message, HttpResponse response, Throwable cause) {
        super(message, cause);
        this.response = response;
    }

    /**
     * @return information about the associated HTTP response
     */
    public HttpResponse response() {
        return response;
    }

    /**
     * @return the HTTP response body
     */
    public Object body() {
        return body;
    }
}
