// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * An exception thrown for an invalid response with custom error information.
 *
 * @see ConnectException
 * @see HttpRequestException
 */
public class ServiceRequestException extends AzureException {

    /**
     * The HTTP response value.
     */
    private Object value;

    /**
     * Initializes a new instance of the ServiceRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     */
    public ServiceRequestException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the ServiceRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param value the deserialized response value
     */
    public ServiceRequestException(String message, Object value) {
        super(message);
        this.value = value;
    }

    /**
     * Initializes a new instance of the ServiceRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param cause the Throwable which caused the creation of this ServiceRequestException
     */
    public ServiceRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @return the HTTP response value
     */
    public Object value() {
        return value;
    }
}
