// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CommunicationError;
import com.azure.communication.callingserver.models.ServerCallingError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link CommunicationError} and
 * {@link ServerCallingError}.
 */
public final class ServerCallingErrorConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.CommunicationError} to {@link ServerCallingError}.
     */
    public static ServerCallingError convert(CommunicationError obj) {
        if (obj == null) {
            return null;
        }

        List<ServerCallingError> details = new ArrayList<ServerCallingError>();

        if (obj.getDetails() != null) {
            details = obj.getDetails()
                .stream()
                .map(detail -> convert(detail))
                .collect(Collectors.toList());
        }

        ServerCallingError serverCallingError = new ServerCallingError(
            obj.getMessage(),
            obj.getCode(),
            obj.getTarget(),
            details,
            convert(obj.getInnerError())
        );

        return serverCallingError;
    }

    private ServerCallingErrorConverter() {
    }
}

