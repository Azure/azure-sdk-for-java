// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.RemoveParticipantFailed;
import com.azure.communication.callautomation.models.events.RemoveParticipantSucceeded;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;

/**
 * The result of a remove participant event.
 */
@Immutable
public final class RemoveParticipantEventResult {
    private final boolean isSuccess;
    private final RemoveParticipantSucceeded successResult;
    private final RemoveParticipantFailed failureResult;
    private final CommunicationIdentifier participant;

    /**
     * Initializes a new instance of RemoveParticipantEventResult.
     *
     * @param isSuccess the success status of the remove participant operation.
     * @param successResult the remove participant succeeded event.
     * @param failureResult the remove participant failed event.
     * @param participant the participant.
     */
    RemoveParticipantEventResult(boolean isSuccess, RemoveParticipantSucceeded successResult,
        RemoveParticipantFailed failureResult, CommunicationIdentifier participant) {
        this.isSuccess = isSuccess;
        this.successResult = successResult;
        this.failureResult = failureResult;
        this.participant = participant;
    }

    /**
     * Gets the success status of the remove participant operation.
     *
     * @return the success status of the remove participant operation.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the remove participant succeeded event.
     *
     * @return the remove participant succeeded event.
     */
    public RemoveParticipantSucceeded getSuccessResult() {
        return successResult;
    }

    /**
     * Gets the remove participant failed event.
     *
     * @return the remove participant failed event.
     */
    public RemoveParticipantFailed getFailureResult() {
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
