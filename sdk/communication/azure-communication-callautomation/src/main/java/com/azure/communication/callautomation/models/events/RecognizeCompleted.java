// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.util.Optional;

import com.azure.communication.callautomation.models.CallMediaRecognitionType;
import com.azure.communication.callautomation.models.CollectTonesResult;
import com.azure.communication.callautomation.models.RecognizeResult;
import com.azure.communication.callautomation.models.CollectChoiceResult;
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

    /*
     * Defines the result for RecognizeChoice
     */
    @JsonProperty(value = "choiceResult", access = JsonProperty.Access.WRITE_ONLY)
    private CollectChoiceResult collectChoiceResult;

    /**
     * Get the collectToneResult or choiceResult property.
     *
     * @return the recognizeResult value.
     */
    public Optional<RecognizeResult> getRecognizeResult() {
        if (this.recognitionType == CallMediaRecognitionType.DTMF) {
            return Optional.ofNullable(this.collectTonesResult);

        } else if (this.recognitionType == CallMediaRecognitionType.CHOICES) {
            return Optional.ofNullable(this.collectChoiceResult);
        }
        return Optional.empty();
    }
}
