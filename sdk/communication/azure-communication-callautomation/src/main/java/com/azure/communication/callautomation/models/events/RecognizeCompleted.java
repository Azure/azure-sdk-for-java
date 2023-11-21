// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.CallMediaRecognitionType;
import com.azure.communication.callautomation.models.DtmfResult;
import com.azure.communication.callautomation.models.RecognizeResult;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/** The RecognizeCompleted model. */
@Immutable
public final class RecognizeCompleted extends CallAutomationEventBaseWithReasonCode {

    /*
     * Determines the subtype of the recognize operation.
     * In case of cancel operation this field is not set and is returned
     * empty
     */
    @JsonProperty(value = "recognitionType", access = JsonProperty.Access.WRITE_ONLY)
    private CallMediaRecognitionType recognitionType;

    /*
     * Defines the result for CallMediaRecognitionType = Dtmf
     */
    @JsonProperty(value = "dtmfResult", access = JsonProperty.Access.WRITE_ONLY)
    private DtmfResult dtmfResult;

    /**
     * Get the collectToneResult or choiceResult property.
     *
     * @return the recognizeResult value.
     */
    public Optional<RecognizeResult> getRecognizeResult() {
        if (this.recognitionType == CallMediaRecognitionType.DTMF) {
            return Optional.ofNullable(this.dtmfResult);

        }

        return Optional.empty();
    }
}
