// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.CommunicationIdentifier;

/**
 * The participant in a call.
 */
public final class CommunicationParticipant {

    /**
     * The communication identity of the participant.
     */
    private CommunicationIdentifier identifier;

    /**
     * Get the communication identity of the participant.
     *
     * @return the communication identity of the participant object itself
     */
    public CommunicationIdentifier getIdentifier() {
        return this.identifier;
    }

    /**
     * Set the communication identity of the participant.
     *
     * @param identifier the communication identity of the participant to set.
     * @return the CommunicationParticipant object itself.
     */
    public CommunicationParticipant setIdentifier(CommunicationIdentifier identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * The The participant id.
     */
    private String participantId;

    /**
     * Get the participant id.
     *
     * @return the participant id value.
     */
    public String getParticipantId() {
        return this.participantId;
    }

    /**
     * Set the participant id.
     *
     * @param participantId the participant id need to set.
     * @return the CommunicationParticipant object itself.
     */
    public CommunicationParticipant setParticipantId(String participantId) {
        this.participantId = participantId;
        return this;
    }

    /**
     * Is participant muted.
     */
    private boolean isMuted;

    /**
     * Get is participant muted.
     *
     * @return the communication identity of the participant object itself
     */
    public boolean getIsMuted() {
        return this.isMuted;
    }

    /**
     * Set the value is participant muted.
     *
     * @param isMuted Is participant muted
     * @return the CommunicationParticipant object itself.
     */
    public CommunicationParticipant setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        return this;
    }

    /**
     * Initializes a new instance of CommunicationParticipant.
     * @param communicationIdentifier Communication Identifier.
     * @param participantId Participant Id.
     * @param isMuted Is participant muted.
     */
    public CommunicationParticipant(CommunicationIdentifier communicationIdentifier, String participantId, boolean isMuted)
    {
        if (communicationIdentifier == null)
        {
            throw new IllegalArgumentException(String.format("object '%s' cannot be null", communicationIdentifier.getClass().getName()));
        }
        if (participantId == null)
        {
            throw new IllegalArgumentException(String.format("object '%s' cannot be null", participantId.getClass().getName()));
        }

        this.identifier = communicationIdentifier;
        this.participantId = participantId;
        this.isMuted = isMuted;
    }
}
