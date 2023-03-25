// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;

import com.azure.communication.rooms.implementation.models.RoomParticipant;
import com.azure.communication.rooms.models.RoomsCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.rooms.implementation.models.RoomsCollection} and
 * {@link RoomsCollection}.
 */
public final class RoomsCollectionConverter {
    private RoomsCollectionConverter() {
    }

    /**
     * Maps from {@link com.azure.communication.rooms.implementation.models.RoomsCollection} to
     * {@link RoomsCollection}.
     */
    public static RoomsCollection convert(
        com.azure.communication.rooms.implementation.models.RoomsCollection obj) {

        if (obj == null) {
            return null;
        }

        List<com.azure.communication.rooms.models.RoomParticipant> rooms = new ArrayList<>();

        if (obj.getValue() != null) {
            rooms = obj.getValue()
                .stream()
                .map((room) -> RoomParticipantConverter.convert(room))
                .collect(Collectors.toList());
        }

        RoomsCollection RoomsCollection = new RoomsCollection().setValue(rooms);
        return RoomsCollection;
    }

    /**
     * Maps from {@link RoomsCollection} to {@link com.azure.communication.rooms.implementation.models.RoomsCollection}.
     */
    public static com.azure.communication.rooms.implementation.models.RoomsCollection convert(
        RoomsCollection obj) {

        if (obj == null) {
            return null;
        }

        List<RoomParticipant> rooms = new ArrayList<>();

        if (obj.getValue() != null) {
            rooms = obj.getValue()
                .stream()
                .map((room) -> RoomParticipantConverter.convert(room))
                .collect(Collectors.toList());
        }

        com.azure.communication.rooms.implementation.models.RoomsCollection RoomsCollection = new com.azure.communication.rooms.implementation.models.RoomsCollection().setParticipants(participants);
        return RoomsCollection;
    }
}
