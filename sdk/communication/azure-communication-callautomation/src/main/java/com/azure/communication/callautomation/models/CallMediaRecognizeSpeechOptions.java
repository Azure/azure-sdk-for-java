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
     * Set the recognizeInputType property: Determines the type of the recognition.
     *
     * @param recognizeInputType the recognizeInputType value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setRecognizeInputType(RecognizeInputType recognizeInputType) {
        super.setRecognizeInputType(recognizeInputType);
        return this;
    }

    /**
     * Set the playPrompt property: The source of the audio to be played for recognition.
     *
     * @param playPrompt the playPrompt value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setPlayPrompt(PlaySource playPrompt) {
        super.setPlayPrompt(playPrompt);
        return this;
    }

    /**
     * Set the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param interruptCallMediaOperation the interruptCallMediaOperation value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setInterruptCallMediaOperation(Boolean interruptCallMediaOperation) {
        super.setInterruptCallMediaOperation(interruptCallMediaOperation);
        return this;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setOperationContext(String operationContext) {
        super.setOperationContext(operationContext);
        return this;
    }

    /**
     * Set the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPrompt the interruptPrompt value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setInterruptPrompt(
        Boolean interruptPrompt) {
        super.setInterruptPrompt(interruptPrompt);
        return this;
    }

    /**
     * Set the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeout the initialSilenceTimeout value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setInitialSilenceTimeout(Duration initialSilenceTimeout) {
        super.setInitialSilenceTimeout(initialSilenceTimeout);
        return this;
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
