// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.MoveParticipantFailed;
import com.azure.communication.callautomation.models.events.MoveParticipantSucceeded;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;

/**
 * The result of a move participant event.
 */
@Immutable
public final class MoveParticipantEventResult {
    private final boolean isSuccess;
    private final MoveParticipantSucceeded successResult;
    private final MoveParticipantFailed failureResult;
    private final CommunicationIdentifier participant;
    private final String fromCall;

    /**
     * Initializes a new instance of MoveParticipantEventResult.
     *
     * @param isSuccess the success status of the move participant operation.
     * @param successResult the move participant succeeded event.
     * @param failureResult the move participant failed event.
     * @param participant the participant.
     * @param fromCall the call connection id for the call to move the participant from.
     */
    MoveParticipantEventResult(boolean isSuccess, MoveParticipantSucceeded successResult,
        MoveParticipantFailed failureResult, CommunicationIdentifier participant, String fromCall) {
        this.isSuccess = isSuccess;
        this.successResult = successResult;
        this.failureResult = failureResult;
        this.participant = participant;
        this.fromCall = fromCall;
    }

    /**
     * Gets the success status of the move participant operation.
     *
     * @return the success status of the move participant operation.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the move participant succeeded event.
     *
     * @return the move participant succeeded event.
     */
    public MoveParticipantSucceeded getSuccessResult() {
        return successResult;
    }

    /**
     * Gets the move participant failed event.
     *
     * @return the move participant failed event.
     */
    public MoveParticipantFailed getFailureResult() {
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

    /**
     * Gets the call connection id for the call to move the participant from.
     *
     * @return the from call connection id.
     */
    public String getFromCall() {
        return fromCall;
    }
}
