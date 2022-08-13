// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callautomation.implementation.models.CommunicationError;
import com.azure.communication.callautomation.implementation.models.CommunicationErrorResponse;
import com.azure.core.annotation.Immutable;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown for an invalid response with {@link CallAutomationError} information.
 **/
@Immutable
public final class CallAutomationErrorException extends HttpResponseException {
    /**
     * Initializes a new instance of the CallingServerResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    CallAutomationErrorException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the CallAutomationErrorException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    CallAutomationErrorException(String message, HttpResponse response, CallAutomationError value) {
        super(message, response, value);
    }

    /**
     * Public constructor
     */
    public CallAutomationErrorException() {
        super(null, null, null);
    }

    static {
        ErrorConstructorProxy.setAccessor(
            new ErrorConstructorProxy.ErrorConstructorAccessor() {
                @Override
                public CallAutomationErrorException create(HttpResponseException internalHeaders) {
                    CallAutomationError error = null;
                    if (internalHeaders.getValue() != null) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                        CommunicationErrorResponse communicationErrorResponse = objectMapper.convertValue(
                            internalHeaders.getValue(), CommunicationErrorResponse.class);
                        error = convert(communicationErrorResponse.getError());
                    }
                    return new CallAutomationErrorException(internalHeaders.getMessage(), internalHeaders.getResponse(), error);
                }
            });
    }

    /**
     * Maps from {@link CommunicationError} to {@link CallAutomationError}.
     */
    private static CallAutomationError convert(CommunicationError communicationError) {
        if (communicationError == null) {
            return null;
        }

        List<CallAutomationError> details = new ArrayList<>();

        if (communicationError.getDetails() != null) {
            details = communicationError
                .getDetails()
                .stream()
                .map(CallAutomationErrorException::convert)
                .collect(Collectors.toList());
        }

        return new CallAutomationError(
            communicationError.getMessage(),
            communicationError.getCode(),
            communicationError.getTarget(),
            details,
            convert(communicationError.getInnererror())
        );
    }

    @Override
    public CallAutomationError getValue() {
        return (CallAutomationError) super.getValue();
    }
}
