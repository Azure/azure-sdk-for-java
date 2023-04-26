// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.time.Duration;

import com.azure.communication.common.CommunicationIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The Recognize configurations specific for Continuous Speech Recognition. **/
public class CallMediaRecognizeSpeechOptions extends CallMediaRecognizeOptions {
    /*
     * The length of end silence when user stops speaking and cogservice send
     * response.
     */
    @JsonProperty(value = "endSilenceTimeoutInMs")
    private Duration endSilenceTimeoutInMs;

    /**
     * Get the endSilenceTimeoutInMs property: The length of end silence when user stops speaking and cogservice send
     * response.
     *
     * @return the endSilenceTimeoutInMs value.
     */
    public Duration getEndSilenceTimeoutInMs() {
        return this.endSilenceTimeoutInMs;
    }

    /**
     * Initializes a CallMediaRecognizeSpeechOptions object.
     *
     * @param targetParticipant Target participant of continuous speech recognition.
     * @param endSilenceTimeoutInMs the endSilenceTimeoutInMs value to set.
     */
    public CallMediaRecognizeSpeechOptions(CommunicationIdentifier targetParticipant, Duration endSilenceTimeoutInMs) {
        super(RecognizeInputType.SPEECH, targetParticipant);
        this.endSilenceTimeoutInMs = endSilenceTimeoutInMs;
    }
}
