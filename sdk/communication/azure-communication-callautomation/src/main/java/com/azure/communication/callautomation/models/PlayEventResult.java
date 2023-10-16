// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.PlayCompleted;
import com.azure.communication.callautomation.models.events.PlayFailed;
import com.azure.core.annotation.Immutable;

/**
 * The result of a play event.
 */
@Immutable
public final class PlayEventResult {
    private final boolean isSuccess;
    private final PlayCompleted successResult;
    private final PlayFailed failureResult;

    /**
     * Initializes a new instance of PlayEventResult.
     *
     * @param isSuccess the success status of the play operation.
     * @param successResult the play succeeded event.
     * @param failureResult the play failed event.
     */
    PlayEventResult(boolean isSuccess, PlayCompleted successResult, PlayFailed failureResult) {
        this.isSuccess = isSuccess;
        this.successResult = successResult;
        this.failureResult = failureResult;
    }

    /**
     * Gets the success status of the play operation.
     *
     * @return the success status of the play operation.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the play succeeded event.
     *
     * @return the play succeeded event.
     */
    public PlayCompleted getSuccessResult() {
        return successResult;
    }

    /**
     * Gets the play failed event.
     *
     * @return the play failed event.
     */
    public PlayFailed getFailureResult() {
        return failureResult;
    }

}
