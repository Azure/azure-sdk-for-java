/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The TranscriptModerationBodyItemTermsItem model.
 */
public class TranscriptModerationBodyItemTermsItem {
    /**
     * Index of the word.
     */
    @JsonProperty(value = "Index", required = true)
    private int index;

    /**
     * Detected word.
     */
    @JsonProperty(value = "Term", required = true)
    private String term;

    /**
     * Get the index value.
     *
     * @return the index value
     */
    public int index() {
        return this.index;
    }

    /**
     * Set the index value.
     *
     * @param index the index value to set
     * @return the TranscriptModerationBodyItemTermsItem object itself.
     */
    public TranscriptModerationBodyItemTermsItem withIndex(int index) {
        this.index = index;
        return this;
    }

    /**
     * Get the term value.
     *
     * @return the term value
     */
    public String term() {
        return this.term;
    }

    /**
     * Set the term value.
     *
     * @param term the term value to set
     * @return the TranscriptModerationBodyItemTermsItem object itself.
     */
    public TranscriptModerationBodyItemTermsItem withTerm(String term) {
        this.term = term;
        return this;
    }

}
