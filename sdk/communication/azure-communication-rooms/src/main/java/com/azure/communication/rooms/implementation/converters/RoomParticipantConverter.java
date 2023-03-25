// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;
import com.azure.communication.rooms.models.RoomParticipant;

/**
 * A converter between {@link com.azure.communication.rooms.implementation.models.RoomParticipant} and
 * {@link RoomParticipant}.
 */
public final class RoomParticipantConverter {
    /**
     * Maps from {com.azure.communication.room.implementation.models.RoomParticipant} to {@link RoomParticipant}.
     */
    public static RoomParticipant convert(com.azure.communication.rooms.implementation.models.RoomParticipant obj) {
        if (obj == null) {
            return null;
        }

        RoomParticipant roomParticipant = new RoomParticipant()
            .setCommunicationIdentifier(CommunicationIdentifierConverter.convert(obj.getRawId()))
            .setRole(RoleTypeConverter.convert(obj.getRole()));

        return roomParticipant;
    }

    /**
     * Maps from {RoomParticipant} to {@link com.azure.communication.room.implementation.models.RoomParticipant}.
     */
    public static com.azure.communication.rooms.implementation.models.RoomParticipant convert(RoomParticipant obj) {
        if (obj == null) {
            return null;
        }

        com.azure.communication.rooms.implementation.models.RoomParticipant roomParticipant
            = new com.azure.communication.rooms.implementation.models.RoomParticipant()
                .setRawId(CommunicationIdentifierConverter.convert(obj.getCommunicationIdentifier()))
                .setRole(RoleTypeConverter.convert(obj.getRole()));

        return roomParticipant;
    }

    private RoomParticipantConverter() {
    }
}
