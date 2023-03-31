// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

/** The RoomParticipant model. */
@Fluent
public final class InvitedRoomParticipant {

    private final CommunicationIdentifier communicationIdentifier;
    private ParticipantRole role;

    /**
     * The default constructor of InvitedRoomParticipant.
     *
     * @param communicationIdentifier The communication identifier.
     */
    public InvitedRoomParticipant(CommunicationIdentifier communicationIdentifier) {
        this.communicationIdentifier = communicationIdentifier;
    }


    /**
     * Get the communicationIdentifier property: Identifies a participant in Azure Communication services. A participant
     * is, for example, a phone number or an Azure communication user. This model must be interpreted as a union: Apart
     * from rawId, at most one further property may be set.
     *
     * @return the communicationIdentifier value.
     */
    public CommunicationIdentifier getCommunicationIdentifier() {
        return this.communicationIdentifier;
    }

    /**
     * Get the role property: The Role of a room participant.
     *
     * @return the role value.
     */
    public ParticipantRole getRole() {
        return this.role;
    }

    /**
     * Set the role property: The Role of a room participant.
     *
     * @param role the role value to set.
     * @return the RoomParticipant object itself.
     */
    public InvitedRoomParticipant setRole(ParticipantRole role) {
        this.role = role;
        return this;
    }
}
