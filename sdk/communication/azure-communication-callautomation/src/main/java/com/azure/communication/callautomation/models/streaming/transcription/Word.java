// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.streaming.transcription;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The result for each word of the phrase
 */
public class Word {

    /*
     * Text in the phrase.
     */
    @JsonProperty(value = "text")
    private String text;

    /*
     * The word's position within the phrase.
     */
    @JsonProperty(value = "offset")
    private long offset;

    /*
     * Duration in ticks. 1 tick = 100 nanoseconds.
     */
    @JsonProperty(value = "duration")
    private long duration;

    /**
     * Get the text property.
     *
     * @return the text value.
     */
    public String getText() {
        return text;
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
     * Get the duration property.
     *
     * @return the duration value.
     */
    public long getDuration() {
        return duration;
    }
}
