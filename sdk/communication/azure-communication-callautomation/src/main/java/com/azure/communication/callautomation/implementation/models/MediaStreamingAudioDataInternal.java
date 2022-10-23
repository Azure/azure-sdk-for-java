// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** The MediaStreamingAudioInternal model. */
public final class MediaStreamingAudioDataInternal {

    /*
     * The audio data.
     */
    @JsonProperty(value = "data")
    private String data;

    /*
     * The timestamp of when the media was sourced.
     */
    @JsonProperty(value = "timestamp")
    private String timestamp;

    /*
     * The participantId.
     */
    @JsonProperty(value = "participantRawID")
    private String participantRawID;

    /*
     * Indicates if the received audio buffer contains only silence.
     */
    @JsonProperty(value = "silent")
    private boolean silent;

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
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Get the participantRawID property.
     *
     * @return the participantRawID value.
     */
    public String getParticipantRawID() {
        return participantRawID;
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
