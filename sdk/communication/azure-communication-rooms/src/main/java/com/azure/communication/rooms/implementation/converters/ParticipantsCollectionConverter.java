// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;

import com.azure.communication.rooms.implementation.models.RoomParticipant;
import com.azure.communication.rooms.models.ParticipantsCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.rooms.implementation.models.ParticipantsCollection} and
 * {@link ParticipantsCollection}.
 */
public final class ParticipantsCollectionConverter {
    private ParticipantsCollectionConverter() {
    }

    /**
     * Maps from {@link com.azure.communication.rooms.implementation.models.ParticipantsCollection} to
     * {@link ParticipantsCollection}.
     */
    public static ParticipantsCollection convert(
        com.azure.communication.rooms.implementation.models.ParticipantsCollection obj) {

        if (obj == null) {
            return null;
        }

        List<com.azure.communication.rooms.models.RoomParticipant> participants = new ArrayList<>();

        if (obj.getParticipants() != null) {
            participants = obj.getParticipants()
                .stream()
                .map((participant) -> RoomParticipantConverter.convert(participant))
                .collect(Collectors.toList());
        }

        ParticipantsCollection participantsCollection = new ParticipantsCollection().setParticipants(participants);
        return participantsCollection;
    }

    /**
     * Maps from {@link ParticipantsCollection} to {@link com.azure.communication.rooms.implementation.models.ParticipantsCollection}.
     */
    public static com.azure.communication.rooms.implementation.models.ParticipantsCollection convert(
        ParticipantsCollection obj) {

        if (obj == null) {
            return null;
        }

        List<RoomParticipant> participants = new ArrayList<>();

        if (obj.getParticipants() != null) {
            participants = obj.getParticipants()
                .stream()
                .map((participant) -> RoomParticipantConverter.convert(participant))
                .collect(Collectors.toList());
        }

        com.azure.communication.rooms.implementation.models.ParticipantsCollection participantsCollection = new com.azure.communication.rooms.implementation.models.ParticipantsCollection().setParticipants(participants);
        return participantsCollection;
    }
}
