// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/** The MediaStreamingAudio model. */
public class MediaStreamingAudioData extends MediaStreamingPackageBase {

    /*
     * The audio data.
     */
    private final String data;

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

    /**
     * The constructor
     *
     * @param data The audio data.
     * @param timestamp The timestamp of when the media was sourced.
     * @param participantRawID The participantId.
     * @param silent Indicates if the received audio buffer contains only silence.
     */
    MediaStreamingAudioData(String data, String timestamp, String participantRawID, boolean silent) {
        this.data = data;
        this.timestamp = OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.participant = new CommunicationUserIdentifier(participantRawID);
        this.silent = silent;
    }

    /**
     * Get the data property.
     *
     * @return the data value.
     */
    public String getData() {
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
}
