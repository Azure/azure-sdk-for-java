// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.CallMediaRecognitionType;
import com.azure.communication.callautomation.models.CollectTonesResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.azure.core.annotation.Immutable;

/** The RecognizeCompleted model. */
@Immutable
public final class RecognizeCompleted extends CallAutomationEventWithReasonCodeBase {

    /*
     * Determines the sub-type of the recognize operation.
     * In case of cancel operation the this field is not set and is returned
     * empty
     */
    @JsonProperty(value = "recognitionType", access = JsonProperty.Access.WRITE_ONLY)
    private CallMediaRecognitionType recognitionType;

    /*
     * Defines the result for CallMediaRecognitionType = Dtmf
     */
    @JsonProperty(value = "collectTonesResult", access = JsonProperty.Access.WRITE_ONLY)
    private CollectTonesResult collectTonesResult;

    /**
     * Get the recognitionType property: Determines the sub-type of the recognize operation. In case of cancel operation
     * the this field is not set and is returned empty.
     *
     * @return the recognitionType value.
     */
    public CallMediaRecognitionType getRecognitionType() {
        return this.recognitionType;
    }

    /**
     * Get the collectTonesResult property: Defines the result for CallMediaRecognitionType = Dtmf.
     *
     * @return the collectTonesResult value.
     */
    public CollectTonesResult getCollectTonesResult() {
        return this.collectTonesResult;
    }
}
