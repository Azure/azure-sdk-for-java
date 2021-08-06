// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CommunicationError;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.models.CallingServerError;
import com.azure.communication.callingserver.models.CallingServerErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link CommunicationError} and {@link CallingServerError}.
 */
public final class CallingServerErrorConverter {
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
                .map(CallingServerErrorConverter::convert)
                .collect(Collectors.toList());
        }

        return new CallingServerError(
            communicationError.getMessage(),
            communicationError.getCode(),
            communicationError.getTarget(),
            details,
            convert(communicationError.getInnerError())
        );
    }

    /**
     * Maps from {@link CommunicationErrorResponseException} to {@link CallingServerErrorException}.
     */
    public static CallingServerErrorException translateException(CommunicationErrorResponseException exception) {
        CallingServerError error = null;
        if (exception.getValue() != null) {
            error = CallingServerErrorConverter.convert(exception.getValue().getError());
        }
        return new CallingServerErrorException(exception.getMessage(), exception.getResponse(), error);
    }

    private CallingServerErrorConverter() {
    }
}

