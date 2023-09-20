// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;

import com.azure.communication.rooms.models.CommunicationRoom;

/**
 * A converter between {@link com.azure.communication.rooms.implementation.models.RoomModel} and
 * {@link CommunicationRoom}.
 */
public final class RoomModelConverter {
    /**
     * Maps from {com.azure.communication.rooms.implementation.models.RoomModel} to {@link CommunicationRoom}.
     */
    public static CommunicationRoom convert(com.azure.communication.rooms.implementation.models.RoomModel room) {
        if (room == null) {
            return null;
        }

        CommunicationRoom communicationRoom = new CommunicationRoom(room.getId(), room.getValidFrom(), room.getValidUntil(), room.getCreatedAt());

        return communicationRoom;
    }

    /**
     * Maps from {@link CommunicationRoom} to {com.azure.communication.rooms.implementation.models.RoomModel}.
    */
    public static com.azure.communication.rooms.implementation.models.RoomModel convert(CommunicationRoom communicationRoom) {
        if (communicationRoom == null) {
            return null;
        }

        com.azure.communication.rooms.implementation.models.RoomModel room = new com.azure.communication.rooms.implementation.models.RoomModel()
            .setId(communicationRoom.getRoomId())
            .setValidFrom(communicationRoom.getValidFrom())
            .setValidUntil(communicationRoom.getValidUntil())
            .setCreatedAt(communicationRoom.getCreatedAt());

        return room;

    }
}
