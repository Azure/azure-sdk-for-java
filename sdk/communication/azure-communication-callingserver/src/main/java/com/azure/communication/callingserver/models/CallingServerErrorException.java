// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callingserver.implementation.models.CommunicationError;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponse;
import com.azure.core.annotation.Immutable;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    CallingServerErrorException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the CallingServerErrorException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    CallingServerErrorException(String message, HttpResponse response, CallingServerError value) {
        super(message, response, value);
    }

    /**
     * Public constructor
     */
    public CallingServerErrorException() {
        super(null, null, null);
    }

    static {
        ErrorConstructorProxy.setAccessor(
            new ErrorConstructorProxy.ErrorConstructorAccessor() {
                @Override
                public CallingServerErrorException create(HttpResponseException internalHeaders) {
                    CallingServerError error = null;
                    if (internalHeaders.getValue() != null) {
                        error = convert(((CommunicationErrorResponse) internalHeaders.getValue()).getError());
                    }
                    return new CallingServerErrorException(internalHeaders.getMessage(), internalHeaders.getResponse(), error);
                }
            });
    }

    /**
     * Maps from {@link CommunicationError} to {@link CallingServerError}.
     */
    private static CallingServerError convert(CommunicationError communicationError) {
        if (communicationError == null) {
            return null;
        }

        List<CallingServerError> details = new ArrayList<>();

        if (communicationError.getDetails() != null) {
            details = communicationError
                .getDetails()
                .stream()
                .map(CallingServerErrorException::convert)
                .collect(Collectors.toList());
        }

        return new CallingServerError(
            communicationError.getMessage(),
            communicationError.getCode(),
            communicationError.getTarget(),
            details,
            convert(communicationError.getInnererror())
        );
    }

    @Override
    public CallingServerError getValue() {
        return (CallingServerError) super.getValue();
    }
}
