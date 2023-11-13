// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

/** The Recognize configurations specific for Continuous Speech Recognition. **/
@Fluent
public class CallMediaRecognizeSpeechOptions extends CallMediaRecognizeOptions {
    /*
     * The length of end silence when user stops speaking and cogservice send
     * response.
     */
    @JsonProperty(value = "endSilenceTimeout")
    private Duration endSilenceTimeout;

    /*
     * Speech language to be recognized, If not set default is en-US
     */
    @JsonProperty(value = "speechLanguage")
    private String speechLanguage;

    /*
     * Endpoint where the custom model was deployed.
     */
    @JsonProperty(value = "speechRecognitionModelEndpointId")
    private String speechRecognitionModelEndpointId;

    /**
     * Get the endSilenceTimeout property: The length of end silence when user stops speaking and cogservice send
     * response.
     *
     * @return the endSilenceTimeout value.
     */
    public Duration getEndSilenceTimeout() {
        return this.endSilenceTimeout;
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
     * Set the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param stopCurrentOperations the stopCurrentOperations value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    @Override
    public CallMediaRecognizeSpeechOptions setStopCurrentOperations(Boolean stopCurrentOperations) {
        super.setStopCurrentOperations(stopCurrentOperations);
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
     * Set the interruptPrompt property: If set to true, the participant has the ability to
     * interrupt the prompt by pressing a digit and the service will start recognizing.
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
     * Set the speech language property.
     * @param speechLanguage the speechLanguage value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    public CallMediaRecognizeSpeechOptions setSpeechLanguage(String speechLanguage) {
        this.speechLanguage = speechLanguage;
        return this;
    }

    /**
     * Get the speech language property.
     *
     * @return the speech language.
     */
    public String getSpeechLanguage() {
        return this.speechLanguage;
    }

    /**
     * Get the speechRecognitionModelEndpointId property: Endpoint where the custom model was deployed.
     *
     * @return the speechRecognitionModelEndpointId value.
     */
    public String getSpeechRecognitionModelEndpointId() {
        return this.speechRecognitionModelEndpointId;
    }

    /**
     * Set the speechRecognitionModelEndpointId property: Endpoint where the custom model was deployed.
     *
     * @param speechRecognitionModelEndpointId the speechRecognitionModelEndpointId value to set.
     * @return the CallMediaRecognizeSpeechOptions object itself.
     */
    public CallMediaRecognizeSpeechOptions setSpeechRecognitionModelEndpointId(String speechRecognitionModelEndpointId) {
        this.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
        return this;
    }

    /**
     * Initializes a CallMediaRecognizeSpeechOptions object.
     *
     * @param targetParticipant Target participant of continuous speech recognition.
     * @param endSilenceTimeout the endSilenceTimeout value to set.
     */
    public CallMediaRecognizeSpeechOptions(CommunicationIdentifier targetParticipant, Duration endSilenceTimeout) {
        super(RecognizeInputType.SPEECH, targetParticipant);
        this.endSilenceTimeout = endSilenceTimeout;
    }
}
