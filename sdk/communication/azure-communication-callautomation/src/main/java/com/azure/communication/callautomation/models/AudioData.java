// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.AudioDataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.AudioDataConverter;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/** The MediaStreamingAudio model. */
public final class AudioData extends StreamingData<AudioData> {

    private static final ClientLogger LOGGER = new ClientLogger(AudioData.class);
    
    /*
     * The audio data.
     */
    private final byte[] data;

    /*
     * The timestamp of when the media was sourced.
     */
    private final OffsetDateTime timestamp;

    /*
     * The participantId.
     */
    private final CommunicationIdentifier participant;

    /*
     * Indicates if the received audio buffer contains only silence.
     */
    private final boolean silent;

    static {
        AudioDataContructorProxy.setAccessor(
            new AudioDataContructorProxy.AudioDataContructorProxyAccessor() {
                @Override
                public AudioData create(AudioDataConverter internalData) {
                    return new AudioData(internalData);
                }

                @Override
                public AudioData create(byte[] data) {
                    return new AudioData(data);
                }
            }
        );
    }

    /**
     * Package-private constructor of the class, used internally.
     *
     * @param internalData The audiodataconvertor
     */
    AudioData(AudioDataConverter internalData) {
        this.data = Base64.getDecoder().decode(internalData.getData());
        this.timestamp = OffsetDateTime.parse(internalData.getTimestamp(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        if (internalData.getParticipantRawID() != null && !internalData.getParticipantRawID().isEmpty()) {
            this.participant = CommunicationIdentifier.fromRawId(internalData.getParticipantRawID());
        } else {
            participant = null;
        }
        this.silent = internalData.isSilent();
    }

    /**
     * The constructor
     */
    public AudioData() {
        this.data = null;
        this.timestamp = null;
        this.participant = null;
        this.silent = false;
    }

    /**
     * The constructor
     *
     * @param data The audio data.
     */
    AudioData(byte[] data) {
        this.data = data;
        this.timestamp = null;
        this.participant = null;
        this.silent = false;
    }

    /**
     * Get the data property.
     *
     * @return the data value.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get the timestamp property.
     *
     * @return the timestamp value.
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Get the participantRawID property.
     *
     * @return the participantRawID value.
     */
    public CommunicationIdentifier getParticipant() {
        return participant;
    }

    /**
     * Get the silent property.
     *
     * @return the silent value.
     */
    public boolean isSilent() {
        return silent;
    }

    @Override
    public AudioData parse(String data) {
        // Implementation for parsing audio data
        try (JsonReader jsonReader = JsonProviders.createReader(data)) {
            return jsonReader.readObject(reader -> {
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("audioData".equals(fieldName)) {
                        // Possible return of AudioData
                        final AudioDataConverter audioInternal = AudioDataConverter.fromJson(reader);
                        if (audioInternal != null) {
                            return AudioDataContructorProxy.create(audioInternal);
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
}
