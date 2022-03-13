// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;

/**
 * The participant in a call.
 */
@Immutable
public final class CallParticipant {

    /**
     * The communication identity of the participant.
     */
    private final CommunicationIdentifier identifier;

    /**
     * The participant id.
     */
    private final String participantId;

    /**
     * Is participant muted.
     */
    private final boolean isMuted;

    /**
     * Get the communication identity of the participant.
     *
     * @return the communication identity of the participant object itself
     */
    public CommunicationIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * Get the participant id.
     *
     * @return the participant id value.
     */
    public String getParticipantId() {
        return participantId;
    }

    /**
     * Get is participant muted.
     *
     * @return the communication identity of the participant object itself
     */
    public boolean isMuted() {
        return isMuted;
    }

    /**
     * Initializes a new instance of CallParticipant.
     *
     * @param communicationIdentifier Communication Identifier.
     * @param participantId Participant Id.
     * @param isMuted Is participant muted.
     * @throws IllegalArgumentException if either parameter is null.
     */
    public CallParticipant(
        CommunicationIdentifier communicationIdentifier,
        String participantId,
        boolean isMuted) {
        if (communicationIdentifier == null) {
            throw new IllegalArgumentException("object communicationIdentifier cannot be null");
        }
        if (participantId == null) {
            throw new IllegalArgumentException("object participantId cannot be null");
        }

        this.identifier = communicationIdentifier;
        this.participantId = participantId;
        this.isMuted = isMuted;
    }
}
