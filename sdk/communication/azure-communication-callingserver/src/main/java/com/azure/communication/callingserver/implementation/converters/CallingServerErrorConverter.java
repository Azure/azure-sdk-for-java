// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CommunicationError;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.communication.callingserver.models.CallingServerError;
import com.azure.communication.callingserver.models.CallingServerErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public final class CallingServerErrorConverter {
    public static CallingServerError convert(CommunicationError obj) {
        if (obj == null) {
            return null;
        }

        List<CallingServerError> details = new ArrayList<>();

        if (obj.getDetails() != null) {
            details = obj.getDetails()
                .stream()
                .map(CallingServerErrorConverter::convert)
                .collect(Collectors.toList());
        }

        return new CallingServerError(
            obj.getMessage(),
            obj.getCode(),
            obj.getTarget(),
            details,
            convert(obj.getInnerError())
        );
    }

    public static CallingServerErrorException translateException(CommunicationErrorException exception) {
        CallingServerError error = null;
        if (exception.getValue() != null) {
            error = CallingServerErrorConverter.convert(exception.getValue());
        }
        return new CallingServerErrorException(exception.getMessage(), exception.getResponse(), error);
    }

    private CallingServerErrorConverter() {
    }
}

