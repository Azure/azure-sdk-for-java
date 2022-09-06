// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

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
    @JsonProperty(value = "interruptPromptAndStartRecognition")
    private Boolean interruptPromptAndStartRecognition;

    /*
     * Time to wait for first input after prompt (if any).
     */
    @JsonProperty(value = "initialSilenceTimeoutInSeconds")
    private Duration initialSilenceTimeoutInSeconds;

    /*
     * Target participant of DTFM tone recognition.
     */
    @JsonProperty(value = "targetParticipant")
    private CommunicationIdentifier targetParticipant;

    /**
     * Initializes a CallMediaRecognizeOptions object.
     * @param recognizeInputType What input the operation should recognize.
     */
    public CallMediaRecognizeOptions(RecognizeInputType recognizeInputType) {
        this.recognizeInputType = recognizeInputType;
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
     * Get the interruptPromptAndStartRecognition property: Determines if we interrupt the prompt and start recognizing.
     *
     * @return the interruptPromptAndStartRecognition value.
     */
    public Boolean isInterruptPromptAndStartRecognition() {
        return this.interruptPromptAndStartRecognition;
    }

    /**
     * Set the interruptPromptAndStartRecognition property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPromptAndStartRecognition the interruptPromptAndStartRecognition value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public CallMediaRecognizeOptions setInterruptPromptAndStartRecognition(
        Boolean interruptPromptAndStartRecognition) {
        this.interruptPromptAndStartRecognition = interruptPromptAndStartRecognition;
        return this;
    }

    /**
     * Get the initialSilenceTimeoutInSeconds property: Time to wait for first input after prompt (if any).
     *
     * @return the initialSilenceTimeoutInSeconds value.
     */
    public Duration getInitialSilenceTimeoutInSeconds() {
        return this.initialSilenceTimeoutInSeconds;
    }

    /**
     * Set the initialSilenceTimeoutInSeconds property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeoutInSeconds the initialSilenceTimeoutInSeconds value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public CallMediaRecognizeOptions setInitialSilenceTimeoutInSeconds(Duration initialSilenceTimeoutInSeconds) {
        this.initialSilenceTimeoutInSeconds = initialSilenceTimeoutInSeconds;
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
     * Set the targetParticipant property: Target participant of DTFM tone recognition.
     *
     * @param targetParticipant the targetParticipant value to set.
     * @return the RecognizeConfigurations object itself.
     */
    public CallMediaRecognizeOptions setTargetParticipant(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        return this;
    }
}
