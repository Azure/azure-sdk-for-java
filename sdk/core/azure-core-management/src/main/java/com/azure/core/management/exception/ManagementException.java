// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown for an invalid response with custom error information.
 */
public class ManagementException extends HttpResponseException {

    /**Serial version id for this class*/
    private static final long serialVersionUID = 1L;

    /**
     * Initializes a new instance of the {@link ManagementException} class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ManagementException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the {@link ManagementException} class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ManagementException(String message, HttpResponse response, ManagementError value) {
        super(message, response, value);
    }

    @Override
    public ManagementError getValue() {
        return (ManagementError) super.getValue();
    }

    @Override
    public String toString() {
        String message = super.toString();
        if (getValue() != null && getValue().getMessage() != null) {
            message = message + ": " + getValue().getMessage();
        }
        return message;
    }
}
