// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.CreateCallFailed;
import com.azure.core.annotation.Immutable;

/**
 * The result of a create call event.
 */
@Immutable
public final class CreateCallEventResult {
    private final boolean isSuccess;
    private final CallConnected successResult;
    private final CreateCallFailed failureResult;

    /**
     * Initializes a new instance of CreateCallEventResult.
     *
     * @param isSuccess the success status of the create call operation.
     * @param successResult the call connected success event.
     */
    CreateCallEventResult(boolean isSuccess, CallConnected successResult, CreateCallFailed failureResult) {
        this.isSuccess = isSuccess;
        this.successResult = successResult;
        this.failureResult = failureResult;
    }

    /**
     * Gets the success status of the create call operation.
     *
     * @return the success status of the create call operation.
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

    /**
     * Gets the create call failure event.
     *
     * @return the create call failure event.
     */
    public CreateCallFailed getFailureResult() {
        return failureResult;
    }
}
