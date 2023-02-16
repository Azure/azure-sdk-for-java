// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The RoomParticipant model. */
@Fluent
public final class RoomParticipant {
    /*
     * Identifies a participant in Azure Communication services. A participant
     * is, for example, a phone number or an Azure communication user. This
     * model must be interpreted as a union: Apart from rawId, at most one
     * further property may be set.
     */
    @JsonProperty(value = "communicationIdentifier", required = true)
    private CommunicationIdentifier communicationIdentifier;

    /*
     * The Role of a room participant.
     */
    @JsonProperty(value = "role")
    private RoleType role;

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
     * Set the communicationIdentifier property: Identifies a participant in Azure Communication services. A participant
     * is, for example, a phone number or an Azure communication user. This model must be interpreted as a union: Apart
     * from rawId, at most one further property may be set.
     *
     * @param communicationIdentifier the communicationIdentifier value to set.
     * @return the RoomParticipant object itself.
     */
    public  RoomParticipant setCommunicationIdentifier(CommunicationIdentifier communicationIdentifier) {
        this.communicationIdentifier = communicationIdentifier;
        return this;
    }

    /**
     * Get the role property: The Role of a room participant.
     *
     * @return the role value.
     */
    public RoleType getRole() {
        return this.role;
    }

    /**
     * Set the role property: The Role of a room participant.
     *
     * @param role the role value to set.
     * @return the RoomParticipant object itself.
     */
    public RoomParticipant setRole(RoleType role) {
        this.role = role;
        return this;
    }
}
