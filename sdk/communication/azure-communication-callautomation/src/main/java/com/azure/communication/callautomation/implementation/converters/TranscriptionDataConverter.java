// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The TranscriptionDataInternal model.
 */
public final class TranscriptionDataConverter {

    /*
     * The display form of the recognized word
     */
    @JsonProperty(value = "text")
    private String text;

    /*
     * The format of text
     */
    @JsonProperty(value = "format")
    private String format;

    /*
     * Confidence of recognition of the whole phrase, from 0.0 (no confidence) to 1.0 (full confidence)
     */
    @JsonProperty(value = "confidence")
    private double confidence;

    /*
     * The position of this payload
     */
    @JsonProperty(value = "offset")
    private long offset;

     /*
     * Duration in ticks. 1 tick = 100 nanoseconds.
     */
    @JsonProperty(value = "duration")
    private long duration;

    /*
     * The participantId.
     */
    @JsonProperty(value = "participantRawID")
    private String participantRawID;

    /*
     * Status of the result of transcription
     */
    @JsonProperty(value = "resultStatus")
    private String resultStatus;

    /**
     * Get the text property.
     *
     * @return the text value.
     */
    public String getText() {
        return text;
    }

    /**
     * Get the format property.
     *
     * @return the format value.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Get the confidence property.
     *
     * @return the confidence value.
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Get the duration property.
     *
     * @return the duration value.
     */
    public long getDuration() {
        return duration;
    }
  /**
     * Get the offset property.
     *
     * @return the offset value.
     */
    public long getOffset() {
        return offset;
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
     * Get the resultStatus property.
     *
     * @return the resultStatus value.
     */
    public String getResultStatus() {
        return resultStatus;
    }
}
