// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.time.Duration;
import java.util.List;

import com.azure.communication.common.CommunicationIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The Recognize configurations specific for Continuous Speech or DTMF Recognition. **/
public class CallMediaRecognizeSpeechOrDtmfOptions extends CallMediaRecognizeOptions {
    /*
     * The length of end silence when user stops speaking and cogservice send
     * response.
     */
    @JsonProperty(value = "endSilenceTimeoutInMs")
    private Duration endSilenceTimeoutInMs;

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
    public CallMediaRecognizeSpeechOrDtmfOptions setInterToneTimeout(Duration interToneTimeout) {
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
     * @return the DtmfConfigurationsInternal object itself.
     */
    public CallMediaRecognizeSpeechOrDtmfOptions setStopTones(List<DtmfTone> stopDtmfTones) {
        this.stopDtmfTones = stopDtmfTones;
        return this;
    }

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
     * Initializes a CallMediaRecognizeSpeechOrDtmfOptions object.
     *
     * @param targetParticipant Target participant of continuous speech recognition.
     * @param maxTonesToCollect Maximum number of DTMF tones to be collected.
     * @param endSilenceTimeoutInMs the endSilenceTimeoutInMs value to set.
     */
    public CallMediaRecognizeSpeechOrDtmfOptions(CommunicationIdentifier targetParticipant, int maxTonesToCollect, Duration endSilenceTimeoutInMs) {
        super(RecognizeInputType.SPEECH_OR_DTMF, targetParticipant);
        this.endSilenceTimeoutInMs = endSilenceTimeoutInMs;
        this.interToneTimeout = Duration.ofSeconds(2);
        this.maxTonesToCollect = maxTonesToCollect;
    }
}

