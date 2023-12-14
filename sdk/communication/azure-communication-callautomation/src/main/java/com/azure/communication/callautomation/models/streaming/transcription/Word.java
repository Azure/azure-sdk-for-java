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
}
