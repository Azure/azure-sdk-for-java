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
    private final AudioChannelType channels;

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
        super(StreamingDataKind.AUDIO_METADATA);
        this.mediaSubscriptionId = internalData.getMediaSubscriptionId();
        this.encoding = internalData.getEncoding();
        this.sampleRate = internalData.getSampleRate();
        this.channels = convertToChannelsEnum(internalData.getChannels());
    }

    /**
     * A unique identifier for the media subscription.
     * Get the mediaSubscriptionId property.
     *
     * @return the mediaSubscriptionId value.
     */
    public String getMediaSubscriptionId() {
        return mediaSubscriptionId;
    }

    /**
     * The format used to encode the audio. Currently, only "pcm" (Pulse Code Modulation) is supported.
     * Get the encoding property.
     *
     * @return the encoding value.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * The number of samples per second in the audio. Supported values are 16kHz or 24kHz.
     * Get the sampleRate property.
     *
     * @return the sampleRate value.
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Specifies the number of audio channels in the audio configuration. Currently, only "mono" (single channel) is supported.
     * Get the channels property.
     *
     * @return the channels value.
     */
    public AudioChannelType getChannelType() {
        return channels;
    }

    /**
     * Converting the channels int type to enum
     * @param channels channels id for the audio
     * @return Channels enum
     */
    private AudioChannelType convertToChannelsEnum(Integer channels) {
        if (1 == channels) {
            return AudioChannelType.MONO;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unsupported Channels "));
        }
    }
}
