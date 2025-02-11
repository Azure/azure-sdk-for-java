// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.AddParticipantFailed;
import com.azure.communication.callautomation.models.events.AddParticipantSucceeded;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;

/**
 * The result of an add participant event.
 */
@Immutable
public final class AddParticipantEventResult {
    private final boolean isSuccess;
    private final AddParticipantSucceeded successResult;
    private final AddParticipantFailed failureResult;
    private final CommunicationIdentifier participant;

    /**
     * Initializes a new instance of AddParticipantEventResult.
     *
     * @param isSuccess the success status of the add participant operation.
     * @param successResult the add participant succeeded event.
     * @param failureResult the add participant failed event.
     * @param participant the participant.
     */
    AddParticipantEventResult(boolean isSuccess, AddParticipantSucceeded successResult,
        AddParticipantFailed failureResult, CommunicationIdentifier participant) {
        this.isSuccess = isSuccess;
        this.successResult = successResult;
        this.failureResult = failureResult;
        this.participant = participant;
    }

    /**
     * Gets the success status of the add participant operation.
     *
     * @return the success status of the add participant operation.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the add participant succeeded event.
     *
     * @return the add participant succeeded event.
     */
    public AddParticipantSucceeded getSuccessResult() {
        return successResult;
    }

    /**
     * Gets the add participant failed event.
     *
     * @return the add participant failed event.
     */
    public AddParticipantFailed getFailureResult() {
        return failureResult;
    }

    /**
     * Gets the participant.
     *
     * @return the participant.
     */
    public CommunicationIdentifier getParticipant() {
        return participant;
    }
}
