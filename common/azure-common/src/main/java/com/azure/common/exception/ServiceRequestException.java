// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * An exception thrown for an invalid response with custom error information.
 */
public class ServiceRequestException extends AzureException {
    /**
     * Information about the associated HTTP response.
     */
    private HttpResponse response;

    /**
     * The HTTP response value.
     */
    private Object value;

    /**
     * Initializes a new instance of the ServiceRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ServiceRequestException(String message, HttpResponse response) {
        super(message);
        this.response = response;
    }

    /**
     * Initializes a new instance of the ServiceRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ServiceRequestException(String message, HttpResponse response, Object value) {
        super(message);
        this.response = response;
        this.value = value;
    }

    /**
     * Initializes a new instance of the ServiceRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ServiceRequestException
     */
    public ServiceRequestException(String message, HttpResponse response, Throwable cause) {
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
