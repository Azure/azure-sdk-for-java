// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown on rooms specific errors
 */
public class RoomsErrorResponseException extends HttpResponseException {
    /**
     * Initializes a new instance of the RoomsErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public RoomsErrorResponseException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the RoomsErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public RoomsErrorResponseException(
        String message, HttpResponse response, RoomsError value) {
        super(message, response, value);
    }

    @Override
    public RoomsError getValue() {
        return (RoomsError) super.getValue();
    }
}
