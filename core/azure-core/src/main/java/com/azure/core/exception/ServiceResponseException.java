// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

import com.azure.core.http.HttpResponse;

/**
 * A runtime exception indicating service response failure caused by one of the following scenarios:
 * 1. The request was sent, but the client failed to understand the response. (Not in the right format)
 * 2. The connection may have timed out. These errors can be retried for idempotent or
 *    safe operations.
 */
public class ServiceResponseException extends AzureException {

    /**
     * Information about the associated HTTP response.
     */
    private HttpResponse response;

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     */
    public ServiceResponseException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message.
     * @param cause the Throwable which caused the creation of this ServiceResponseException.
     */
    public ServiceResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the Http response received from Azure service
     */
    public ServiceResponseException(final String message, final HttpResponse response) {
        super(message);
        this.response = response;
    }


    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the Http response received from Azure service
     * @param cause the Throwable which caused the creation of this ServiceResponseException
     */
    public ServiceResponseException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, cause);
        this.response = response;
    }

    /**
     * @return information about the associated HTTP response
     */
    public HttpResponse response() {
        return response;
    }

}
