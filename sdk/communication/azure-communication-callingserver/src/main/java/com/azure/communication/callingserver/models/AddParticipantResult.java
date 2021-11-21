// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The add participant result. */
@Immutable
public final class AddParticipantResult {
    /*
     * The id of the added participant.
     */
    private final String participantId;

    /**
     * Get the participantId property: The id of the added participant.
     *
     * @return the participantId value.
     */
    public String getParticipantId() {
        return this.participantId;
    }

    /**
     * Initializes a new instance of AddParticipantResult.
     *
     * @param participantId the participantId value.
     */
    public AddParticipantResult(String participantId) {
        this.participantId = participantId;
    }
}
