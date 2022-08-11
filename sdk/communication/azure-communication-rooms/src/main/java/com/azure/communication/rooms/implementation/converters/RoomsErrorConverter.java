// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;

import com.azure.communication.rooms.models.RoomsError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.rooms.implementation.models.CommunicationError} and
 * {@link RoomsError}.
 */
public final class RoomsErrorConverter {
    /**
     * Maps from {com.azure.communication.rooms.implementation.models.CommunicationError} to {@link RoomsError}.
     */
    public static RoomsError convert(com.azure.communication.rooms.implementation.models.CommunicationError error) {
        if (error == null) {
            return null;
        }

        List<RoomsError> details = new ArrayList<RoomsError>();

        if (error.getDetails() != null) {
            details = error.getDetails()
                .stream()
                .map(detail -> convert(detail))
                .collect(Collectors.toList());
        }

        RoomsError roomsError = new RoomsError(
            error.getMessage(),
            error.getCode(),
            error.getTarget(),
            details,
            convert(error.getInnerError())
        );

        return roomsError;
    }

    private RoomsErrorConverter() {
    }
}
