// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import com.azure.core.annotation.Immutable;

/**
 * The result of a start recognizing event.
 */
@Immutable
public final class StartRecognizingEventResult {
    private final boolean isSuccess;
    private final RecognizeCompleted successResult;
    private final RecognizeFailed failureResult;

    /**
     * Initializes a new instance of StartRecognizingEventResult.
     *
     * @param isSuccess the success status of the start recognizing operation.
     * @param successResult the start recognizing succeeded event.
     * @param failureResult the start recognizing failed event.
     */
    StartRecognizingEventResult(boolean isSuccess, RecognizeCompleted successResult, RecognizeFailed failureResult) {
        this.isSuccess = isSuccess;
        this.successResult = successResult;
        this.failureResult = failureResult;
    }

    /**
     * Gets the success status of the start recognizing operation.
     *
     * @return the success status of the start recognizing operation.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the start recognizing succeeded event.
     *
     * @return the start recognizing succeeded event.
     */
    public RecognizeCompleted getSuccessResult() {
        return successResult;
    }

    /**
     * Gets the start recognizing failed event.
     *
     * @return the start recognizing failed event.
     */
    public RecognizeFailed getFailureResult() {
        return failureResult;
    }
}
