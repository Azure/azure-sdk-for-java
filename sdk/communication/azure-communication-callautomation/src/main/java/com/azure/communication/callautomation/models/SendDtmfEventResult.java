// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.SendDtmfCompleted;
import com.azure.communication.callautomation.models.events.SendDtmfFailed;
import com.azure.core.annotation.Immutable;

/**
 * The result of a send dtmf event.
 */
@Immutable
public final class SendDtmfEventResult {
    private final boolean isSuccess;
    private final SendDtmfCompleted successResult;
    private final SendDtmfFailed failureResult;

    /**
     * Initializes a new instance of SendDtmfEventResult.
     *
     * @param isSuccess the success status of the send dtmf operation.
     * @param successResult the send dtmf succeeded event.
     * @param failureResult the send dtmf failed event.
     */
    SendDtmfEventResult(boolean isSuccess, SendDtmfCompleted successResult, SendDtmfFailed failureResult) {
        this.isSuccess = isSuccess;
        this.successResult = successResult;
        this.failureResult = failureResult;
    }

    /**
     * Gets the success status of the send dtmf operation.
     *
     * @return the success status of the send dtmf operation.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the send dtmf succeeded event.
     *
     * @return the send dtmf succeeded event.
     */
    public SendDtmfCompleted getSuccessResult() {
        return successResult;
    }

    /**
     * Gets the send dtmf failed event.
     *
     * @return the send dtmf failed event.
     */
    public SendDtmfFailed getFailureResult() {
        return failureResult;
    }
}
