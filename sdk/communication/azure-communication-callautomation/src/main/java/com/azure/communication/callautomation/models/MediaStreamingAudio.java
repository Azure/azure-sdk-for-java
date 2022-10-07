// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import java.time.OffsetDateTime;

/** The MediaStreamingAudio model. */
public class MediaStreamingAudio extends MediaStreamingPackageBase {

    /*
     * The audio data.
     */
    private byte[] data;

    /*
     * The timestamp of when the media was sourced.
     */
    private OffsetDateTime timestamp;

    /*
     * The participantId.
     */
    private CommunicationIdentifier participant;

    /*
     * Indicates if the received audio buffer contains only silence.
     */
    private boolean silent;

    /**
     * The constructor
     *
     * @param data
     * @param timestamp
     * @param participant
     * @param silent
     */
    public MediaStreamingAudio(byte[] data, OffsetDateTime timestamp, CommunicationIdentifier participant, boolean silent) {
        this.data = data;
        this.timestamp = timestamp;
        this.participant = participant;
        this.silent = silent;
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
}
