// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.azure.communication.rooms.implementation.converters.ParticipantRoleConverter;
import com.azure.communication.rooms.implementation.converters.RoomModelConverter;

public class ConverterTests {
    
    @Test
    void testConvertRoomModel() {
        com.azure.communication.rooms.implementation.models.RoomModel room = new com.azure.communication.rooms.implementation.models.RoomModel();
        room.setId("12345");
        room.setValidFrom(OffsetDateTime.now());
        room.setValidUntil(OffsetDateTime.now().plusMonths(2));
        assertNotNull(RoomModelConverter.convert(room));
        room = null;
        assertNull(RoomModelConverter.convert(room));
    } 

    @Test
    void testConvertRoomParticipantRole() {
        com.azure.communication.rooms.implementation.models.ParticipantRole role = com.azure.communication.rooms.implementation.models.ParticipantRole.ATTENDEE;
        assertNotNull(ParticipantRoleConverter.convert(role));
        role = null;
        assertNull(ParticipantRoleConverter.convert(role));
    }
}
