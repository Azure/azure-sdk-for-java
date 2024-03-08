// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.List;

/** The Recognize configurations specific for Recognize Choice. **/
@Fluent
public final class CallMediaRecognizeChoiceOptions extends CallMediaRecognizeOptions {
    /*
     * List of recognition choices.
     */
    private final List<RecognitionChoice> choices;

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
     * Get the list of recognition choices.
     *
     * @return the list of recognition choices.
     */
    public List<RecognitionChoice> getChoices() {
        return this.choices;
    }

    /**
     * Set the speech language property.
     * @param speechLanguage the interToneTimeout value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    public CallMediaRecognizeChoiceOptions setSpeechLanguage(String speechLanguage) {
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
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    public CallMediaRecognizeChoiceOptions setSpeechRecognitionModelEndpointId(String speechRecognitionModelEndpointId) {
        this.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
        return this;
    }

    /**
     * Set the recognizeInputType property: Determines the type of the recognition.
     *
     * @param recognizeInputType the recognizeInputType value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setRecognizeInputType(RecognizeInputType recognizeInputType) {
        super.setRecognizeInputType(recognizeInputType);
        return this;
    }

    /**
     * Set the playPrompt property: The source of the audio to be played for recognition.
     *
     * @param playPrompt the playPrompt value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setPlayPrompt(PlaySource playPrompt) {
        super.setPlayPrompt(playPrompt);
        return this;
    }

    /**
     * Set the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param interruptCallMediaOperation the interruptCallMediaOperation value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setInterruptCallMediaOperation(Boolean interruptCallMediaOperation) {
        super.setInterruptCallMediaOperation(interruptCallMediaOperation);
        return this;
    }

    /**
     * Set the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param stopCurrentOperations the stopCurrentOperations value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setStopCurrentOperations(Boolean stopCurrentOperations) {
        super.setStopCurrentOperations(stopCurrentOperations);
        return this;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setOperationContext(String operationContext) {
        super.setOperationContext(operationContext);
        return this;
    }

    /**
     * Set the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPrompt the interruptPrompt value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setInterruptPrompt(
        Boolean interruptPrompt) {
        super.setInterruptPrompt(interruptPrompt);
        return this;
    }

    /**
     * Set the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeout the initialSilenceTimeout value to set.
     * @return the CallMediaRecognizeChoiceOptions object itself.
     */
    @Override
    public CallMediaRecognizeChoiceOptions setInitialSilenceTimeout(Duration initialSilenceTimeout) {
        super.setInitialSilenceTimeout(initialSilenceTimeout);
        return this;
    }

    /**
     * Initializes a CallMediaRecognizeDtmfOptions object.
     *
     * @param targetParticipant Target participant of DTFM tone recognition.
     * @param choices Maximum number of DTMF tones to be collected.
     */
    public CallMediaRecognizeChoiceOptions(CommunicationIdentifier targetParticipant,  List<RecognitionChoice> choices) {
        super(RecognizeInputType.CHOICES, targetParticipant);
        this.choices = choices;
    }
}
