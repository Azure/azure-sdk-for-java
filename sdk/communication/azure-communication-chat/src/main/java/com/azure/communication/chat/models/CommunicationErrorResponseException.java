// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/** Exception thrown for an invalid response with CommunicationErrorResponse information. */
public final class CommunicationErrorResponseException extends HttpResponseException {

    /**
     * Initializes a new instance of the CommunicationErrorResponseException class.
     *
     * @param response the HTTP response.
     */
    public CommunicationErrorResponseException(HttpResponse response) {
        super(response);
    }

    /**
     * Initializes a new instance of the CommunicationErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public CommunicationErrorResponseException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the CommunicationErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public CommunicationErrorResponseException(
            String message, HttpResponse response, CommunicationErrorResponse value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the CommunicationErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param cause the inner exception.
     */
    public CommunicationErrorResponseException(String message, HttpResponse response, Throwable cause) {
        super(message, response, cause);
    }

    /**
     * Initializes a new instance of the CommunicationErrorResponseException class.
     *
     * @param response the HTTP response.
     * @param cause the inner exception.
     */
    public CommunicationErrorResponseException(HttpResponse response, Throwable cause) {
        super(response, cause);
    }

    /**
     * Initializes a new instance of the CommunicationErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param cause the inner exception.
     * @param enableSuppression suppress the exception.
     * @param writableStackTrace write to the stacktrace.
     */
    public CommunicationErrorResponseException(
        String message,
        HttpResponse response,
        Throwable cause,
        boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, response, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public CommunicationErrorResponse getValue() {
        return (CommunicationErrorResponse) super.getValue();
    }
}
