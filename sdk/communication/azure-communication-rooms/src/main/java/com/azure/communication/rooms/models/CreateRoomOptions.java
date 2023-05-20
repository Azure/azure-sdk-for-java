// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import java.time.OffsetDateTime;
import java.util.List;

import com.azure.core.annotation.Fluent;

/**
 * The options for creating a room.
 */
@Fluent
public final class CreateRoomOptions {

    /*
     * The timestamp from when the room is open for joining. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time.
     */
    private OffsetDateTime validFrom;

    /*
     * The timestamp from when the room can no longer be joined. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time plus 180 days.
     */
    private OffsetDateTime validUntil;

    /*
     * (Optional) Participants to be invited to the room.
     */
    private Iterable<RoomParticipant> participants;

    /**
     * The default constructor of CreateRoomOptions
     */
    public CreateRoomOptions() {
    }

    /**
     * Set the validFrom property: The timestamp from when the room is open for joining. The timestamp is in RFC3339
     * format: `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time.
     *
     * @param validFrom The starting time of the room.
     * @return The CreateRoomOptions object itself.
     */
    public CreateRoomOptions setValidFrom(OffsetDateTime validFrom) {
        this.validFrom = validFrom;
        return this;
    }

     /**
     * Set the validUntil property: The timestamp from when the room can no longer be joined. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time plus 180 days.
     *
     * @param validUntil The end time of the room.
     * @return The CreateRoomOptions object itself.
     */
    public CreateRoomOptions setValidUntil(OffsetDateTime validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    /**
     * Set the participants property: (Optional) Participants to be invited to the room.
     *
     * @param participants The invited participants.
     * @return The CreateRoomOptions object itself.
     */
    public CreateRoomOptions setParticipants(List<RoomParticipant> participants) {
        this.participants = participants;
        return this;
    }

    /**
     * Get the validFrom property: The timestamp from when the room is open for joining. The timestamp is in RFC3339
     * format: `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time.
     *
     * @return The starting time of the room.
     */
    public OffsetDateTime getValidFrom() {
        return validFrom;
    }

     /**
     * Get the validUntil property: The timestamp from when the room can no longer be joined. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time plus 180 days.
     *
     * @return The end time of the room.
     */
    public OffsetDateTime getValidUntil() {
        return validUntil;
    }

    /**
     * Get the participants property: (Optional) Participants to be invited to the room.
     *
     * @return The invited participants.
     */
    public Iterable<RoomParticipant> getParticipants() {
        return participants;
    }
}
