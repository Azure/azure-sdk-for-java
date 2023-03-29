// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Collection of participants in a room. */
@Fluent
public final class ParticipantsCollection {
    /*
     * Room Participants.
     */
    @JsonProperty(value = "participants", required = true)
    private List<RoomParticipant> participants;

    /**
     * Get the participants property: Room Participants.
     *
     * @return the participants value.
     */
    public List<RoomParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * Set the participants property: Room Participants.
     *
     * @param participants the participants value to set.
     * @return the ParticipantsCollection object itself.
     */
    public ParticipantsCollection setParticipants(List<RoomParticipant> participants) {
        this.participants = participants;
        return this;
    }
}
