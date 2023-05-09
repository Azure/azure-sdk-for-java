// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.List;

/** The Recognize configurations specific for Dtmf. **/
@Fluent
public final class CallMediaRecognizeDtmfOptions extends CallMediaRecognizeOptions {

    /*
     * Time to wait between DTMF inputs to stop recognizing.
     */
    @JsonProperty(value = "interToneTimeout")
    private Duration interToneTimeout;

    /*
     * Maximum number of DTMFs to be collected.
     */
    @JsonProperty(value = "maxTonesToCollect")
    private Integer maxTonesToCollect;

    /*
     * List of tones that will stop recognizing.
     */
    @JsonProperty(value = "stopTones")
    private List<DtmfTone> stopDtmfTones;

    /**
     * Get the interToneTimeout property: Time to wait between DTMF inputs to stop recognizing.
     * If left unspecified, the default value is set to 2 seconds.
     *
     * @return the interToneTimeout value.
     */
    public Duration getInterToneTimeout() {
        return this.interToneTimeout;
    }

    /**
     * Set the interToneTimeout property: Time to wait between DTMF inputs to stop recognizing.
     *
     * @param interToneTimeout the interToneTimeout value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public CallMediaRecognizeDtmfOptions setInterToneTimeout(Duration interToneTimeout) {
        this.interToneTimeout = interToneTimeout;
        return this;
    }

    /**
     * Get the maxTonesToCollect property: Maximum number of DTMFs to be collected.
     *
     * @return the maxTonesToCollect value.
     */
    public Integer getMaxTonesToCollect() {
        return this.maxTonesToCollect;
    }

    /**
     * Get the stopTones property: List of tones that will stop recognizing.
     *
     * @return the stopTones value.
     */
    public List<DtmfTone> getStopTones() {
        return this.stopDtmfTones;
    }

    /**
     * Set the stopTones property: List of tones that will stop recognizing.
     *
     * @param stopDtmfTones the stopTones value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    public CallMediaRecognizeDtmfOptions setStopTones(List<DtmfTone> stopDtmfTones) {
        this.stopDtmfTones = stopDtmfTones;
        return this;
    }

    /**
     * Set the recognizeInputType property: Determines the type of the recognition.
     *
     * @param recognizeInputType the recognizeInputType value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setRecognizeInputType(RecognizeInputType recognizeInputType) {
        super.setRecognizeInputType(recognizeInputType);
        return this;
    }

    /**
     * Set the playPrompt property: The source of the audio to be played for recognition.
     *
     * @param playPrompt the playPrompt value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setPlayPrompt(PlaySource playPrompt) {
        super.setPlayPrompt(playPrompt);
        return this;
    }

    /**
     * Set the interruptCallMediaOperation property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param interruptCallMediaOperation the interruptCallMediaOperation value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setInterruptCallMediaOperation(Boolean interruptCallMediaOperation) {
        super.setInterruptCallMediaOperation(interruptCallMediaOperation);
        return this;
    }

    /**
     * Set the stopCurrentOperations property: If set recognize can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param stopCurrentOperations the stopCurrentOperations value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setStopCurrentOperations(Boolean stopCurrentOperations) {
        super.setStopCurrentOperations(stopCurrentOperations);
        return this;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext the operationContext value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setOperationContext(String operationContext) {
        super.setOperationContext(operationContext);
        return this;
    }

    /**
     * Set the interruptPrompt property: Determines if we interrupt the prompt and start recognizing.
     *
     * @param interruptPrompt the interruptPrompt value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setInterruptPrompt(
        Boolean interruptPrompt) {
        super.setInterruptPrompt(interruptPrompt);
        return this;
    }

    /**
     * Set the initialSilenceTimeout property: Time to wait for first input after prompt (if any).
     *
     * @param initialSilenceTimeout the initialSilenceTimeout value to set.
     * @return the CallMediaRecognizeDtmfOptions object itself.
     */
    @Override
    public CallMediaRecognizeDtmfOptions setInitialSilenceTimeout(Duration initialSilenceTimeout) {
        super.setInitialSilenceTimeout(initialSilenceTimeout);
        return this;
    }

    /**
     * Initializes a CallMediaRecognizeDtmfOptions object.
     *
     * @param targetParticipant Target participant of DTFM tone recognition.
     * @param maxTonesToCollect Maximum number of DTMF tones to be collected.
     */
    public CallMediaRecognizeDtmfOptions(CommunicationIdentifier targetParticipant, int maxTonesToCollect) {
        super(RecognizeInputType.DTMF, targetParticipant);
        this.interToneTimeout = Duration.ofSeconds(2);
        this.maxTonesToCollect = maxTonesToCollect;
    }
}
