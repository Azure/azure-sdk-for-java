// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown for an invalid response with {@link CallingServerError} information.
 **/
public class CallingServerResponseException extends HttpResponseException {
    /**
     * Initializes a new instance of the CallingServerResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public CallingServerResponseException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the CallingServerResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public CallingServerResponseException(
        String message, HttpResponse response, CallingServerError value) {
        super(message, response, value);
    }

    @Override
    public CallingServerError getValue() {
        return (CallingServerError) super.getValue();
    }
}
