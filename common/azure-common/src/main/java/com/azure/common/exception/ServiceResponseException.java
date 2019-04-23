// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * A runtime exception indicating service response failure caused by one of the following scenarios:
 * Service is not able to handle a well-format request by some reasons.
 * OR The request was sent, but the client failed to understand the response.
 *
 * These errors may not be safe to retry.
 */
public class ServiceResponseException extends AzureException {

    /**
     * Information about the associated HTTP response.
     */
    private HttpResponse response;

    /**
     * The HTTP response value.
     */
    private Object value;

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ServiceResponseException(String message, HttpResponse response) {
        super(message);
        this.response = response;
    }

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ServiceResponseException(String message, HttpResponse response, Object value) {
        super(message);
        this.response = response;
        this.value = value;
    }

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ServiceRequestException
     */
    public ServiceResponseException(String message, HttpResponse response, Throwable cause) {
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
