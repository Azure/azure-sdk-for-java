// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown on phone number specific errors
 */
public final class IdentityErrorResponseException extends HttpResponseException {
    /**
     * Initializes a new instance of the IdentityErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public IdentityErrorResponseException(
        String message, HttpResponse response, IdentityError value) {
        super(message, response, value);
    }

    @Override
    public IdentityError getValue() {
        return (IdentityError) super.getValue();
    }
}
