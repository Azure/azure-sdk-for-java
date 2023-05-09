// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.List;

/** The Recognize configurations specific for Recognize Choice. **/
@Fluent
public final class CallMediaRecognizeChoiceOptions extends CallMediaRecognizeOptions {
    /*
     * List of recognize choice.
     */
    private final List<RecognizeChoice> recognizeChoices;

    /*
     * Speech language to be recognized, If not set default is en-US
     */
    private String speechLanguage;

    /**
     * Get the list of recognize choice.
     *
     * @return the list of recognize choice.
     */
    public List<RecognizeChoice> getRecognizeChoices() {
        return this.recognizeChoices;
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
     * Get the list of recognize choice.
     *
     * @return the speech language.
     */
    public String getSpeechLanguage() {
        return this.speechLanguage;
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
     * @param recognizeChoices Maximum number of DTMF tones to be collected.
     */
    public CallMediaRecognizeChoiceOptions(CommunicationIdentifier targetParticipant,  List<RecognizeChoice> recognizeChoices) {
        super(RecognizeInputType.CHOICES, targetParticipant);
        this.recognizeChoices = recognizeChoices;
    }
}
