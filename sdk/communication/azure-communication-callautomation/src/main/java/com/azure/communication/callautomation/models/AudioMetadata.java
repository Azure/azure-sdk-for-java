// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;

import com.azure.communication.callautomation.implementation.accesshelpers.AudioMetadataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.AudioMetadataConverter;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

/** The MediaStreamingMetadata model. */
public final class AudioMetadata extends StreamingData<AudioMetadata> {

    private static final ClientLogger LOGGER = new ClientLogger(AudioMetadata.class);

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
    private final Integer sampleRate;

    /*
     * The channels.
     */
    private final Channels channels;

    /*
     * The length.
     */
    private final Integer length;

    static {
        AudioMetadataContructorProxy.setAccessor(
            new AudioMetadataContructorProxy.AudioMetadataContructorProxyAccessor() {
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
        this.channels =  convertToChannelsEnum(internalData.getChannels());
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

    @Override
    public AudioMetadata parse(String data) {
        // Implementation for parsing audio data
        try (JsonReader jsonReader = JsonProviders.createReader(data)) {
            return jsonReader.readObject(reader -> {
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("audioMetadata".equals(fieldName)) {
                        // Possible return of AudioData
                        final AudioMetadataConverter audioMetadataInternal = AudioMetadataConverter.fromJson(reader);
                        if (audioMetadataInternal != null) {
                            return AudioMetadataContructorProxy.create(audioMetadataInternal);
                        } else {
                            return null;
                        }
                    } else {
                        reader.skipChildren();
                    }
                }

                return null; // cases triggered.
            });
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private Channels convertToChannelsEnum(Integer channels) {
        if (0 == channels) {
            return Channels.MONO;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unsupported Channels "));
        }
    }
}
