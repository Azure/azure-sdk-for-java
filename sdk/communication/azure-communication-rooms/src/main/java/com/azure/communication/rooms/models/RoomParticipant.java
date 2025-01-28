// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

/** The RoomParticipant model. */
@Fluent
public final class RoomParticipant {
    /*
     * Identifies a participant in Azure Communication services. A participant
     * is, for example, a phone number or an Azure communication user. This
     * model must be interpreted as a union: Apart from rawId, at most one
     * further property may be set.
     */
    private final CommunicationIdentifier communicationIdentifier;

    /*
     * The Role of a room participant.
     */
    private ParticipantRole role;

    /**
     * Default constructor for Room Participant. Default role is Attendee
     *
     * @param communicationIdentifier The communication identifier.
     */
    public RoomParticipant(CommunicationIdentifier communicationIdentifier) {
        this.communicationIdentifier = communicationIdentifier;
        this.role = ParticipantRole.ATTENDEE;
    }

    /**
    * Set the role property: The role of a room participant. The default value is Attendee.
    *
    * @param role the role value to set.
    * @return the RoomParticipant object itself.
    */
    public RoomParticipant setRole(ParticipantRole role) {
        if (role == null) {
            this.role = ParticipantRole.ATTENDEE;
        } else {
            this.role = role;
        }
        return this;
    }

    /**
     * Get the communicationIdentifier property: Identifies a participant in Azure
     * Communication services. A participant
     * is, for example, a phone number or an Azure communication user. This model
     * must be interpreted as a union: Apart
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
}
