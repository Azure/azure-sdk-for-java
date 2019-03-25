/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

import com.azure.common.http.rest.RestException;
import com.azure.common.http.HttpResponse;

/**
 * Exception thrown for an invalid response with custom error information.
 */
public final class CloudException extends RestException {
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
    public CloudError body() {
        return (CloudError) super.body();
    }

    @Override
    public String toString() {
        String message = super.toString();
        if (body() != null && body().message() != null) {
            message = message + ": " + body().message();
        }
        return message;
    }
}
