// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The options for creating a room.
 */
public final class CreateRoomOptions {

    /**
     * The starting time point of the room.
     */
    private OffsetDateTime validFrom;

    /**
     * The ending time point of the room.
     */
    private OffsetDateTime validUntil;

    /**
     * A list of participants
     */
    private List<InvitedRoomParticipant> participants;

    /**
     * The default constructor of CreateRoomOptions
     */
    public CreateRoomOptions() {
    }

    /**
     * Set the valid starting time point of a room.
     *
     * @param validFrom The starting time of the room.
     * @return The CreateRoomOptions object itself.
     */
    public CreateRoomOptions setValidFrom(OffsetDateTime validFrom) {
        this.validFrom = validFrom;
        return this;
    }

     /**
     * Set the ending time point of a room.
     *
     * @param validUntil The end time of the room.
     * @return The CreateRoomOptions object itself.
     */
    public CreateRoomOptions setValidUntil(OffsetDateTime validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    /**
     * Set the list of invited participants
     *
     * @param participants The invited participants.
     * @return The CreateRoomOptions object itself.
     */
    public CreateRoomOptions setParticipants(List<InvitedRoomParticipant> participants) {
        this.participants = participants;
        return this;
    }

    /**
     * Get the valid starting time point of a room.
     *
     * @return The starting time of the room.
     */
    public OffsetDateTime getValidFrom() {
        return validFrom;
    }

     /**
     * Get the ending time point of a room.
     *
     * @return The end time of the room.
     */
    public OffsetDateTime getValidUntil() {
        return validUntil;
    }

    /**
     * Get the list of invited participants
     *
     * @return The invited participants.
     */
    public List<InvitedRoomParticipant> getParticipants() {
        return participants;
    }
}
