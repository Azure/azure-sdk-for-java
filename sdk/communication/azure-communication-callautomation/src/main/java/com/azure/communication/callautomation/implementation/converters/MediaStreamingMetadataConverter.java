// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.fasterxml.jackson.annotation.JsonProperty;

/** The MediaStreamingMetadataInternal model. */
public final class MediaStreamingMetadataConverter {

    /*
     * The mediaSubscriptionId.
     */
    @JsonProperty(value = "subscriptionId")
    private String mediaSubscriptionId;

    /*
     * The encoding.
     */
    @JsonProperty(value = "encoding")
    private String encoding;

    /*
     * The sampleRate.
     */
    @JsonProperty(value = "sampleRate")
    private int sampleRate;

    /*
     * The channels.
     */
    @JsonProperty(value = "channels")
    private int channels;

    /*
     * The length.
     */
    @JsonProperty(value = "length")
    private int length;

    /**
     * Get the mediaSubscriptionId property.
     *
     * @return the mediaSubscriptionId value.
     */
    public String getMediaSubscriptionId() {
        return mediaSubscriptionId;
    }

    /**
     * Get the encoding property.
     *
     * @return the encoding value.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Get the sampleRate property.
     *
     * @return the sampleRate value.
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Get the channels property.
     *
     * @return the channels value.
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Get the length property.
     *
     * @return the length value.
     */
    public int getLength() {
        return length;
    }
}
