// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

import com.azure.core.http.HttpResponse;

/**
 * The exception thrown when an unsuccessful response is received
 *  with http status code (e.g. 3XX, 4XX, 5XX) from the service request.
 */
public class HttpResponseException extends AzureException {

    /**
     * The HTTP response value.
     */
    private final Object value;

    /**
     * Information about the associated HTTP response.
     */
    private final HttpResponse response;

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public HttpResponseException(final String message, final HttpResponse response) {
        super(message);
        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HTTPResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response received from Azure service
     * @param value the deserialized response value
     */
    public HttpResponseException(final String message, final HttpResponse response, final Object value) {
        super(message);
        this.value = value;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this HttpResponseException
     */
    public HttpResponseException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, cause);
        this.value = null;
        this.response = response;
    }


    /**
     * @return information about the associated HTTP response
     */
    public HttpResponse getResponse() {
        return response;
    }

    /**
     * @return the HTTP response value
     */
    public Object getValue() {
        return value;
    }
}
