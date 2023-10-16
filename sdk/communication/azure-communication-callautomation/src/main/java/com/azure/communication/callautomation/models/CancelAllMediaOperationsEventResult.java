// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.PlayCanceled;
import com.azure.communication.callautomation.models.events.RecognizeCanceled;
import com.azure.core.annotation.Immutable;

/**
 * The result of a cancel all media operations event.
 */
@Immutable
public final class CancelAllMediaOperationsEventResult {
    private final boolean isSuccess;
    private final PlayCanceled playCanceledSucessEvent;
    private final RecognizeCanceled recognizeCanceledSucessEvent;

    /**
     * Initializes a new instance of CancelAllMediaOperationsEventResult.
     *
     * @param isSuccess the success status of the cancel all media operations.
     * @param playCanceledSucessEvent the play canceled success event.
     * @param recognizeCanceledSucessEvent the recognize canceled success event.
     */
    CancelAllMediaOperationsEventResult(boolean isSuccess, PlayCanceled playCanceledSucessEvent, RecognizeCanceled recognizeCanceledSucessEvent) {
        this.isSuccess = isSuccess;
        this.playCanceledSucessEvent = playCanceledSucessEvent;
        this.recognizeCanceledSucessEvent = recognizeCanceledSucessEvent;
    }

    /**
     * Gets the success status of the cancel all media operations.
     *
     * @return the success status of the cancel all media operations.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the play canceled success event.
     *
     * @return the play canceled success event.
     */
    public PlayCanceled getPlayCanceledSucessEvent() {
        return playCanceledSucessEvent;
    }

    /**
     * Gets the recognize canceled success event.
     *
     * @return the recognize canceled success event.
     */
    public RecognizeCanceled getRecognizeCanceledSucessEvent() {
        return recognizeCanceledSucessEvent;
    }
}
