// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.mgmt;

import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpResponse;

/**
 * Exception thrown for an invalid response with custom error information.
 */
public final class CloudException extends ServiceRequestException {
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
    public CloudError value() {
        return (CloudError) super.value();
    }

    @Override
    public String toString() {
        String message = super.toString();
        if (value() != null && value().message() != null) {
            message = message + ": " + value().message();
        }
        return message;
    }
}
