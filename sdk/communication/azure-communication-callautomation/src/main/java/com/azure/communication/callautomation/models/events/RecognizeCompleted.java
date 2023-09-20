// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.util.Optional;

import com.azure.communication.callautomation.models.CallMediaRecognitionType;
import com.azure.communication.callautomation.models.RecognizeResult;
import com.azure.communication.callautomation.models.ChoiceResult;
import com.azure.communication.callautomation.models.DtmfResult;
import com.azure.communication.callautomation.models.SpeechResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.azure.core.annotation.Immutable;

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

    /*
     * Defines the result for CallMediaRecognitionType = Speech or SpeechOrDtmf
     */
    @JsonProperty(value = "speechResult", access = JsonProperty.Access.WRITE_ONLY)
    private SpeechResult speechResult;

    /*
     * Defines the result for RecognizeChoice
     */
    @JsonProperty(value = "choiceResult", access = JsonProperty.Access.WRITE_ONLY)
    private ChoiceResult collectChoiceResult;

    /**
     * Get the collectToneResult or choiceResult property.
     *
     * @return the recognizeResult value.
     */
    public Optional<RecognizeResult> getRecognizeResult() {
        if (this.recognitionType == CallMediaRecognitionType.DTMF) {
            return Optional.ofNullable(this.dtmfResult);

        } else if (this.recognitionType == CallMediaRecognitionType.CHOICES) {
            return Optional.ofNullable(this.collectChoiceResult);
        } else if (this.recognitionType == CallMediaRecognitionType.SPEECH) {
            return Optional.ofNullable(this.speechResult);
        }

        return Optional.empty();
    }
}
