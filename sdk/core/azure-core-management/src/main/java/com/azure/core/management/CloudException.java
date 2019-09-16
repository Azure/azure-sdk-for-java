// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown for an invalid response with custom error information.
 */
public final class CloudException extends HttpResponseException {

    /**Serial version id for this class*/
    private static final long serialVersionUID = 1L;

    /**
     * Initializes a new instance of the CloudException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public CloudException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the CloudException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param body the deserialized response body
     */
    public CloudException(String message, HttpResponse response, CloudError body) {
        super(message, response, body);
    }

    @Override
    public CloudError getValue() {
        return (CloudError) super.getValue();
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
