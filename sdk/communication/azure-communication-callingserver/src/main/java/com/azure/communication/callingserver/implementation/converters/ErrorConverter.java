// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CommunicationError;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponse;
import com.azure.communication.callingserver.models.CallingServerError;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.core.exception.HttpResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link CommunicationError} and {@link CallingServerError}.
 */
public final class ErrorConverter {
    /**
     * Maps from {@link CommunicationError} to {@link CallingServerError}.
     */
    public static CallingServerError convert(CommunicationError communicationError) {
        if (communicationError == null) {
            return null;
        }

        List<CallingServerError> details = new ArrayList<>();

        if (communicationError.getDetails() != null) {
            details = communicationError
                .getDetails()
                .stream()
                .map(ErrorConverter::convert)
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

    /**
     * Maps from {@link HttpResponseException} to {@link CallingServerErrorException}.
     */
    public static CallingServerErrorException translateException(HttpResponseException exception) {
        CallingServerError error = null;
        if (exception.getValue() != null) {
            error = ErrorConverter.convert(((CommunicationErrorResponse) exception.getValue()).getError());
        }
        return new CallingServerErrorException(exception.getMessage(), exception.getResponse(), error);
    }

    private ErrorConverter() {
    }
}

