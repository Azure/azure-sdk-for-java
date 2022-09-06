// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** The Recognize configurations specific for Dtmf. **/
public class CallMediaRecognizeDtmfOptions extends CallMediaRecognizeOptions {

    /*
     * Time to wait between DTMF inputs to stop recognizing.
     */
    @JsonProperty(value = "interToneTimeoutInSeconds")
    private Integer interToneTimeoutInSeconds;

    /*
     * Maximum number of DTMFs to be collected.
     */
    @JsonProperty(value = "maxTonesToCollect")
    private Integer maxTonesToCollect;

    /*
     * List of tones that will stop recognizing.
     */
    @JsonProperty(value = "stopTones")
    private List<StopTones> stopTones;

    /**
     * Get the interToneTimeoutInSeconds property: Time to wait between DTMF inputs to stop recognizing.
     *
     * @return the interToneTimeoutInSeconds value.
     */
    public Integer getInterToneTimeoutInSeconds() {
        return this.interToneTimeoutInSeconds;
    }

    /**
     * Set the interToneTimeoutInSeconds property: Time to wait between DTMF inputs to stop recognizing.
     *
     * @param interToneTimeoutInSeconds the interToneTimeoutInSeconds value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public CallMediaRecognizeDtmfOptions setInterToneTimeoutInSeconds(Integer interToneTimeoutInSeconds) {
        this.interToneTimeoutInSeconds = interToneTimeoutInSeconds;
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
     * Set the maxTonesToCollect property: Maximum number of DTMFs to be collected.
     *
     * @param maxTonesToCollect the maxTonesToCollect value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public CallMediaRecognizeDtmfOptions setMaxTonesToCollect(Integer maxTonesToCollect) {
        this.maxTonesToCollect = maxTonesToCollect;
        return this;
    }

    /**
     * Get the stopTones property: List of tones that will stop recognizing.
     *
     * @return the stopTones value.
     */
    public List<StopTones> getStopTones() {
        return this.stopTones;
    }

    /**
     * Set the stopTones property: List of tones that will stop recognizing.
     *
     * @param stopTones the stopTones value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public CallMediaRecognizeDtmfOptions setStopTones(List<StopTones> stopTones) {
        this.stopTones = stopTones;
        return this;
    }

    /**
     * Initializes a CallMediaRecognizeDtmfOptions object.
     * @param recognizeInputType What input the operation should recognize.
     */
    public CallMediaRecognizeDtmfOptions(RecognizeInputType recognizeInputType) {
        super(recognizeInputType);
    }
}
