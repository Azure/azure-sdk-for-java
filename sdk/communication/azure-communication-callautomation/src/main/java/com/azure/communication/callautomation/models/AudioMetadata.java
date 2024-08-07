// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

/** The MediaStreamingMetadata model. */
public final class AudioMetadata extends StreamingData {

    /*
     * The mediaSubscriptionId.
     */
    private final String mediaSubscriptionId;

    /*
     * The encoding.
     */
    private final String encoding;

    /*
     * The sampleRate.
     */
    private final int sampleRate;

    /*
     * The channels.
     */
    private final int channels;

    /*
     * The length.
     */
    private final int length;

    /**
     * The constructor
     *
     * @param mediaSubscriptionId The mediaSubscriptionId.
     * @param encoding The encoding.
     * @param sampleRate The sampleRate.
     * @param channels The channels.
     * @param length The length.
     */
    public AudioMetadata(String mediaSubscriptionId, String encoding, int sampleRate, int channels, int length) {
        this.mediaSubscriptionId = mediaSubscriptionId;
        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.length = length;
    }

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
