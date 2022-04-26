// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.azure.communication.rooms.implementation.models.RoomParticipantInternal;
import com.azure.core.annotation.Immutable;

/** The CommunicationRoom model. */
@Immutable
public final class CommunicationRoom {
    private final String roomId;
    private final OffsetDateTime validFrom;
    private final OffsetDateTime validUntil;
    private final OffsetDateTime createdTime;
    private final List<RoomParticipant> participants;
    private final Map<String, RoomParticipantInternal> invalidParticipants;

    /**
     * The default constructor of CommunicationRoom.
     *
     * @param roomId The Room Id.
     * @param validFrom The starting time point of the room.
     * @param validUntil The ending time point of the room.
     * @param createdTime The created time point of the room.
     * @param participants The participants of the room.
     * @param invalidParticipants The invalid participants as a returned map from request.
     */
    public CommunicationRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, OffsetDateTime createdTime,
        List<RoomParticipant> participants, Map<String, RoomParticipantInternal> invalidParticipants) {
        this.roomId = roomId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.createdTime = createdTime;
        this.participants = participants;
        this.invalidParticipants = invalidParticipants;
    }

    /**
     * Get the Room Id.
     *
     * @return Room Id.
     */
    public String getRoomId() {
        return this.roomId;
    }

    /**
     * Get the participants of a room.
     *
     * @return The participants of the room.
     */
    public List<RoomParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * Get the valid starting time point of a room.
     *
     * @return The starting time of the room.
     */
    public OffsetDateTime getValidFrom() {
        return this.validFrom;
    }

    /**
     * Get the ending time point of a room.
     *
     * @return The end time of the room.
     */
    public OffsetDateTime getValidUntil() {
        return this.validUntil;
    }

    /**
     * Get the created time of the room.
     *
     * @return The created time of the room.
     */
    public OffsetDateTime getCreatedTime() {
        return this.createdTime;
    }

    /**
     * The invalid participants for create and update operation.
     *
     * @return the invalid participants.
     */
    public Map<String, RoomParticipantInternal> getInvalidParticipants() {
        return this.invalidParticipants;
    }
}

