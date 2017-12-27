/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.TranscriptModerationBodyItemTermsItem;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schema items of the body.
 */
public class TranscriptModerationBodyItemInner {
    /**
     * Timestamp of the image.
     */
    @JsonProperty(value = "Timestamp", required = true)
    private String timestamp;

    /**
     * Optional metadata details.
     */
    @JsonProperty(value = "Terms", required = true)
    private List<TranscriptModerationBodyItemTermsItem> terms;

    /**
     * Get the timestamp value.
     *
     * @return the timestamp value
     */
    public String timestamp() {
        return this.timestamp;
    }

    /**
     * Set the timestamp value.
     *
     * @param timestamp the timestamp value to set
     * @return the TranscriptModerationBodyItemInner object itself.
     */
    public TranscriptModerationBodyItemInner withTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Get the terms value.
     *
     * @return the terms value
     */
    public List<TranscriptModerationBodyItemTermsItem> terms() {
        return this.terms;
    }

    /**
     * Set the terms value.
     *
     * @param terms the terms value to set
     * @return the TranscriptModerationBodyItemInner object itself.
     */
    public TranscriptModerationBodyItemInner withTerms(List<TranscriptModerationBodyItemTermsItem> terms) {
        this.terms = terms;
        return this;
    }

}
