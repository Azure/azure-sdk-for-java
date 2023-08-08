// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

/** Options to configure the Recognize operation **/
public abstract class CallMediaRecognizeOptions {
    /*
     * Determines the type of the recognition.
     */
    @JsonProperty(value = "recognizeInputType", required = true)
    private RecognizeInputType recognizeInputType;

    /*
     * The source of the audio to be played for recognition.
     */
    @JsonProperty(value = "playPrompt")
    private PlaySource playPrompt;

    /*
     * If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     */
    @JsonProperty(value = "interruptCallMediaOperation")
    private Boolean interruptCallMediaOperation;

    /*
     * If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     */
    @JsonProperty(value = "stopCurrentOperations")
    private Boolean stopCurrentOperations;

    /*
     * The value to identify context of the operation.
     */
    @JsonProperty(value = "operationContext")
    private String operationContext;

    /*
     * Determines if we interrupt the prompt and start recognizing.
     */
    @JsonProperty(value = "interruptPrompt")
    private Boolean interruptPrompt;

    /*
     * Time to wait for first input after prompt (if any).
     */
    @JsonProperty(value = "initialSilenceTimeout")
    private Duration initialSilenceTimeout;

    /*
     * Endpoint where the custom model was deployed.
     */
    @JsonProperty(value = "speechModelEndpointId")
    private String speechModelEndpointId;

    /*
     * Target participant of DTFM tone recognition.
     */
    @JsonProperty(value = "targetParticipant")
    private CommunicationIdentifier targetParticipant;

    /**
     * Initializes a CallMediaRecognizeOptions object.
     * @param recognizeInputType What input the operation should recognize.
     * @param targetParticipant Target participant of DTFM tone recognition.
     */
    public CallMediaRecognizeOptions(RecognizeInputType recognizeInputType, CommunicationIdentifier targetParticipant) {
        this.recognizeInputType = recognizeInputType;
        this.targetParticipant = targetParticipant;
        this.initialSilenceTimeout = Duration.ofSeconds(5);
    }

    /**
     * Get the recognizeInputType property: Determines the type of the recognition.
     *
     * @return the recognizeInputType value.
     */
    public RecognizeInputType getRecognizeInputType() {
        return this.recognizeInputType;
    }

    /**
     * Set the recognizeInputType property: Determines the type of the recognition.
     *
     * @param recognizeInputType the recognizeInputType value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setRecognizeInputType(RecognizeInputType recognizeInputType) {
        this.recognizeInputType = recognizeInputType;
        return this;
    }

    /**
     * Get the playPrompt property: The source of the audio to be played for recognition.
     *
     * @return the playPrompt value.
     */
    public PlaySource getPlayPrompt() {
        return this.playPrompt;
    }

    /**
     * Set the playPrompt property: The source of the audio to be played for recognition.
     *
     * @param playPrompt the playPrompt value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setPlayPrompt(PlaySource playPrompt) {
        this.playPrompt = playPrompt;
        return this;
    }

    /**
     * Get the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @return the interruptCallMediaOperation value.
     */
    public Boolean isInterruptCallMediaOperation() {
        return this.interruptCallMediaOperation;
    }

    /**
     * Set the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param interruptCallMediaOperation the interruptCallMediaOperation value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setInterruptCallMediaOperation(Boolean interruptCallMediaOperation) {
        this.interruptCallMediaOperation = interruptCallMediaOperation;
        return this;
    }

    /**
     * Get the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @return the stopCurrentOperations value.
     */
    public Boolean isStopCurrentOperations() {
        return this.stopCurrentOperations;
    }

    /**
     * Set the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param stopCurrentOperations the stopCurrentOperations value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setStopCurrentOperations(Boolean stopCurrentOperations) {
        this.stopCurrentOperations = stopCurrentOperations;
        return this;
    }

    /**
     * Get the operationContext property: The value to identify context of the operation.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the RecognizeRequest object itself.
     */
    public CallMediaRecognizeOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @return the interruptPrompt value.
     */
    public Boolean isInterruptPrompt() {
        return this.interruptPrompt;
    }

    /**
     * Set the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPrompt the interruptPrompt value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public CallMediaRecognizeOptions setInterruptPrompt(
        Boolean interruptPrompt) {
        this.interruptPrompt = interruptPrompt;
        return this;
    }

    /**
     * Get the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @return the initialSilenceTimeout value.
     */
    public Duration getInitialSilenceTimeout() {
        return this.initialSilenceTimeout;
    }

    /**
     * Set the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeout the initialSilenceTimeout value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public CallMediaRecognizeOptions setInitialSilenceTimeout(Duration initialSilenceTimeout) {
        this.initialSilenceTimeout = initialSilenceTimeout;
        return this;
    }

    /**
     * Get the targetParticipant property: Target participant of DTFM tone recognition.
     *
     * @return the targetParticipant value.
     */
    public CommunicationIdentifier getTargetParticipant() {
        return this.targetParticipant;
    }

    /**
     * Get the speech model endpoint id.
     *
     * @return the speech model endpoint id.
     */
    public String getSpeechModelEndpointId() {
        return speechModelEndpointId;
    }

    /**
     * Set the speechModelEndpointId property: Endpoint where the custom model was deployed.
     *
     * @param speechModelEndpointId the initialSilenceTimeout value to set.
     * @return the CallMediaRecognizeSpeechOrDtmfOptions object itself.
     */
    public CallMediaRecognizeOptions setSpeechModelEndpointId(String speechModelEndpointId) {
        this.speechModelEndpointId = speechModelEndpointId;
        return this;
    }
}
