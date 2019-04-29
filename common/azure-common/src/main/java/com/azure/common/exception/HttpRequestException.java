// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * The exception thrown when an unsuccessful response is received
 *  with http status code (e.g. 3XX, 4XX, 5XX) from the service request.
 *
 * @see ServiceRequestException
 */
public class HttpRequestException extends ServiceRequestException {

    /**
     * The HTTP response value.
     */
    private Object value;

    /**
     * Information about the associated HTTP response.
     */
    private final HttpResponse response;

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public HttpRequestException(final String message, final HttpResponse response) {
        super(message);
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public HttpRequestException(final String message, final HttpResponse response, final Object value) {
        super(message);
        this.value = value;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this HttpRequestException
     */
    public HttpRequestException(final String message, final HttpResponse response, final Throwable cause) {
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
     * @return the HTTP response value
     */
    public Object value() {
        return value;
    }
}
