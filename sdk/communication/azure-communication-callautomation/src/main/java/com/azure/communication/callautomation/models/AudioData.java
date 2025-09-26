// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.azure.communication.callautomation.implementation.accesshelpers.AudioDataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.AudioDataConverter;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.util.BinaryData;

/** The data which contains the audio data stream information such as timestamp, data..
 */
public final class AudioData extends StreamingData {
    /*
     * The audio data, encoded as a binary data.
     */
    private final BinaryData data;

    /*
     * The timestamp indicating when the media content was received by the bot, or if the bot is sending media, 
     * the timestamp of when the media was sourced. The format is ISO 8601 (yyyy-mm-ddThh:mm)
     */
    private final OffsetDateTime timestamp;

    /*
     * The raw ID of the participant.
     */
    private final CommunicationIdentifier participant;

    /*
     * Indicates if the received audio buffer contains only silence.
     */
    private final boolean silent;

    static {
        AudioDataContructorProxy.setAccessor(new AudioDataContructorProxy.AudioDataContructorProxyAccessor() {
            @Override
            public AudioData create(AudioDataConverter internalData) {
                return new AudioData(internalData);
            }

            @Override
            public AudioData create(BinaryData data) {
                return new AudioData(data);
            }
        });
    }

    /**
     * Package-private constructor of the class, used internally.
     *
     * @param internalData The audiodataconvertor
     */
    AudioData(AudioDataConverter internalData) {
        super(StreamingDataKind.AUDIO_DATA);
        this.data = BinaryData.fromString(internalData.getData());
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
     *
     * @param data The audio data.
     */
    AudioData(BinaryData data) {
        super(StreamingDataKind.AUDIO_DATA);
        this.data = data;
        this.timestamp = null;
        this.participant = null;
        this.silent = false;
    }

    /**
     * The audio data, encoded audio binary data.
     * Get the data property.
     *
     * @return the encoded audio binary data.
     */
    public BinaryData getData() {
        return data;
    }

    /**
     * The timestamp indicating when the media content was received by the bot, or if the bot is sending media, 
     * the timestamp of when the media was sourced. The format is ISO 8601 (yyyy-mm-ddThh:mm)
     * Get the timestamp property.
     *
     * @return the timestamp value.
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * The raw ID of the participant.
     * Get the participantRawID property.
     *
     * @return the participantRawID value.
     */
    public CommunicationIdentifier getParticipant() {
        return participant;
    }

    /**
     * Indicates if the received audio buffer contains only silence
     * Get the silent property.
     *
     * @return the silent value.
     */
    public boolean isSilent() {
        return silent;
    }
}
