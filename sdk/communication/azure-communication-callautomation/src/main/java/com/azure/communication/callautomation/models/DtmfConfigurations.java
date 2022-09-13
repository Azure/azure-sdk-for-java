// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.List;

/** Options for DTMF recognition. */
@Fluent
public final class DtmfConfigurations {
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
    private List<StopTones> stopTones;

    /**
     * Get the interToneTimeout property: Time to wait between DTMF inputs to stop recognizing.
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
    public DtmfConfigurations setInterToneTimeout(Duration interToneTimeout) {
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
     * Set the maxTonesToCollect property: Maximum number of DTMFs to be collected.
     *
     * @param maxTonesToCollect the maxTonesToCollect value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public DtmfConfigurations setMaxTonesToCollect(Integer maxTonesToCollect) {
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
    public DtmfConfigurations setStopTones(List<StopTones> stopTones) {
        this.stopTones = stopTones;
        return this;
    }
}
