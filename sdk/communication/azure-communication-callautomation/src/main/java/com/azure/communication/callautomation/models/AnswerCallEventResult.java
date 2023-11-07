// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.core.annotation.Immutable;

/**
 * The result of an answer call event.
 */
@Immutable
public final class AnswerCallEventResult {
    private final boolean isSuccess;
    private final CallConnected successResult;

    /**
     * Initializes a new instance of AnswerCallEventResult.
     *
     * @param isSuccess the success status of the answer call operation.
     * @param successResult the call connected success event.
     */
    AnswerCallEventResult(boolean isSuccess, CallConnected successResult) {
        this.isSuccess = isSuccess;
        this.successResult = successResult;
    }

    /**
     * Gets the success status of the answer call operation.
     *
     * @return the success status of the answer call operation.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the call connected success event.
     *
     * @return the call connected success event.
     */
    public CallConnected getSuccessResult() {
        return successResult;
    }

}
