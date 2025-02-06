// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.CallTransferAccepted;
import com.azure.communication.callautomation.models.events.CallTransferFailed;
import com.azure.core.annotation.Immutable;

/**
 * The result of a transfer call to participant event.
 */
@Immutable
public final class TransferCallToParticipantEventResult {
    private final boolean isSuccess;
    private final CallTransferAccepted successResult;
    private final CallTransferFailed failureResult;

    /**
     * Initializes a new instance of TransferCallToParticipantEventResult.
     *
     * @param isSuccess the success status of the transfer call to participant operation.
     * @param successResult the transfer call to participant succeeded event.
     * @param failureResult the transfer call to participant failed event.
     */
    TransferCallToParticipantEventResult(boolean isSuccess, CallTransferAccepted successResult,
        CallTransferFailed failureResult) {
        this.isSuccess = isSuccess;
        this.successResult = successResult;
        this.failureResult = failureResult;
    }

    /**
     * Gets the success status of the transfer call to participant operation.
     *
     * @return the success status of the transfer call to participant operation.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the transfer call to participant succeeded event.
     *
     * @return the transfer call to participant succeeded event.
     */
    public CallTransferAccepted getSuccessResult() {
        return successResult;
    }

    /**
     * Gets the transfer call to participant failed event.
     *
     * @return the transfer call to participant failed event.
     */
    public CallTransferFailed getFailureResult() {
        return failureResult;
    }

}
