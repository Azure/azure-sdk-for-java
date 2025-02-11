// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.AudioMetadataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.AudioMetadataConverter;
import com.azure.core.util.logging.ClientLogger;

/** The MediaStreamingMetadata model. */
public final class AudioMetadata extends StreamingData {

    private static final ClientLogger LOGGER = new ClientLogger(AudioMetadata.class);

    /*
     * A unique identifier for the media subscription.
     */
    private final String mediaSubscriptionId;

    /*
     * The format used to encode the audio. Currently, only "pcm" (Pulse Code Modulation) is supported.
     */
    private final String encoding;

    /*
     * The number of samples per second in the audio. Supported values are 16kHz or 24kHz.
     */
    private final Integer sampleRate;

    /*
     * Specifies the number of audio channels in the audio configuration. Currently, only "mono" (single channel) is supported.
     */
    private final Channels channels;

    /*
     * The size of the audio data being sent, based on the sample rate and duration.
     */
    private final Integer length;

    static {
        AudioMetadataContructorProxy
            .setAccessor(new AudioMetadataContructorProxy.AudioMetadataContructorProxyAccessor() {
                @Override
                public AudioMetadata create(AudioMetadataConverter internalData) {
                    return new AudioMetadata(internalData);
                }
            });
    }

    /**
     * Package-private constructor of the class, used internally.
     *
     * @param internalData The audiodataconvertor
     */
    AudioMetadata(AudioMetadataConverter internalData) {
        this.mediaSubscriptionId = internalData.getMediaSubscriptionId();
        this.encoding = internalData.getEncoding();
        this.sampleRate = internalData.getSampleRate();
        this.channels = convertToChannelsEnum(internalData.getChannels());
        this.length = internalData.getLength();
    }

    /**
     * Creats the audiometadata instance
     */
    public AudioMetadata() {
        this.mediaSubscriptionId = null;
        this.encoding = null;
        this.sampleRate = null;
        this.channels = null;
        this.length = null;
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
    public Channels getChannels() {
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

    /**
     * Converting the channels int type to enum
     * @param channels channels id for the audio
     * @return Channels enum
     */
    private Channels convertToChannelsEnum(Integer channels) {
        if (1 == channels) {
            return Channels.MONO;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unsupported Channels "));
        }
    }
}
