// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.rooms.implementation.converters.ParticipantRoleConverter;
import com.azure.communication.rooms.implementation.models.CreateRoomRequest;
import com.azure.communication.rooms.implementation.models.ParticipantProperties;
import com.azure.communication.rooms.implementation.models.RoomModel;
import com.azure.communication.rooms.implementation.models.UpdateParticipantsRequest;
import com.azure.communication.rooms.implementation.models.UpdateRoomRequest;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to convert service level models to SDK exposed models.
 */
public class Transforms {
    public static CommunicationRoom getCommunicationRoomFromResponse(RoomModel room) {
        return new CommunicationRoom(
            room.getId(),
            room.getValidFrom(),
            room.getValidUntil(),
            room.getCreatedAt(),
            room.isPstnDialOutEnabled());
    }

    /**
     * Translate to create room request.
     *
     * @return The create room request.
     */
    public static CreateRoomRequest toCreateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil,
                                                        Boolean isPstnDialOutEnabled, Iterable<RoomParticipant> participants) {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest();
        if (validFrom != null) {
            createRoomRequest.setValidFrom(validFrom);
        }

        if (validUntil != null) {
            createRoomRequest.setValidUntil(validUntil);
        }

        createRoomRequest.setPstnDialOutEnabled(isPstnDialOutEnabled);

        Map<String, ParticipantProperties> roomParticipants = new HashMap<>();

        if (participants != null) {
            roomParticipants = convertRoomParticipantsToMapForAddOrUpdate(participants);
        }

        if (participants != null) {
            createRoomRequest.setParticipants(roomParticipants);
        }

        return createRoomRequest;
    }

    /**
     * Translate to update room request.
     *
     * @return The update room request.
     */
    public static UpdateRoomRequest toUpdateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil, Boolean isPstnDialOutEnabled) {
        UpdateRoomRequest updateRoomRequest = new UpdateRoomRequest();

        if (validFrom != null) {
            updateRoomRequest.setValidFrom(validFrom);
        }

        if (validUntil != null) {
            updateRoomRequest.setValidUntil(validUntil);
        }

        if (isPstnDialOutEnabled != null) {
            updateRoomRequest.setPstnDialOutEnabled(isPstnDialOutEnabled);
        }

        return updateRoomRequest;
    }

    /**
     * Translate to map for add or update participants.
     *
     * @return Map of participants.
     */
    public static  Map<String, ParticipantProperties> convertRoomParticipantsToMapForAddOrUpdate(
        Iterable<RoomParticipant> participants) {
        Map<String, ParticipantProperties> participantMap = new HashMap<>();

        if (participants != null) {
            for (RoomParticipant participant : participants) {
                participantMap.put(participant.getCommunicationIdentifier().getRawId(),
                    new ParticipantProperties().setRole(ParticipantRoleConverter.convert(participant.getRole())));
            }
        }

        return participantMap;
    }

    /**
     * Translate to map for remove participants.
     *
     * @return Map of participants.
     */
    public static Map<String, ParticipantProperties> convertRoomIdentifiersToMapForRemove(
        Iterable<CommunicationIdentifier> identifiers) {
        Map<String, ParticipantProperties> participantMap = new HashMap<>();

        if (identifiers != null) {
            for (CommunicationIdentifier identifier : identifiers) {
                participantMap.put(identifier.getRawId(), null);
            }
        }

        return participantMap;
    }

    /**
     * Get the update request.
     * @param participantMap The participant map.
     * @return The update request.
     * @throws IOException If the request cannot be serialized.
     */
    public static String getUpdateRequest(Map<String, ParticipantProperties> participantMap) throws IOException {
        Writer json = new StringWriter();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(json)) {
            new UpdateParticipantsRequest().setParticipants(participantMap).toJson(jsonWriter);
        }
        return json.toString();
    }
}
