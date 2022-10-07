// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

public class MediaStreamingMetadata extends MediaStreamingPackageBase {

    /*
     * The mediaSubscriptionId.
     */
    private String mediaSubscriptionId;

    /*
     * The encoding.
     */
    private String encoding;

    /*
     * The sampleRate.
     */
    private int sampleRate;

    /*
     * The channels.
     */
    private int channels;

    /*
     * The length.
     */
    private double length;

    /**
     * The constructor
     *
     * @param mediaSubscriptionId
     * @param encoding
     * @param sampleRate
     * @param channels
     * @param length
     */
    public MediaStreamingMetadata(String mediaSubscriptionId, String encoding, int sampleRate, int channels, double length) {
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
    public double getLength() {
        return length;
    }
}
