// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;

/** The CallParticipant model. */
@Immutable
public final class CallParticipant {
    /*
     * Communication identifier of the participant
     */
    private final CommunicationIdentifier identifier;

    /*
     * Is participant muted
     */
    private final Boolean isMuted;

    /**
     * Constructor of the class
     *
     * @param identifier The communication identifier
     * @param isMuted The value of isMuted
     */
    public CallParticipant(CommunicationIdentifier identifier, Boolean isMuted) {
        this.identifier = identifier;
        this.isMuted = isMuted;
    }

    /**
     * Get the identifier property: Communication identifier of the participant.
     *
     * @return the identifier value.
     */
    public CommunicationIdentifier getIdentifier() {
        return this.identifier;
    }

    /**
     * Get the isMuted property: Is participant muted.
     *
     * @return the isMuted value.
     */
    public Boolean isMuted() {
        return this.isMuted;
    }
}
