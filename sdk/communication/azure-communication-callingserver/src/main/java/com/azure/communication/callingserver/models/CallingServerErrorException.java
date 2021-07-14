// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown for an invalid response with {@link CallingServerError} information.
 **/
@Immutable
public final class CallingServerErrorException extends HttpResponseException {
    /**
     * Initializes a new instance of the CallingServerResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public CallingServerErrorException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the CallingServerErrorException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public CallingServerErrorException(String message, HttpResponse response, CallingServerError value) {
        super(message, response, value);
    }

    @Override
    public CallingServerError getValue() {
        return (CallingServerError) super.getValue();
    }
}
