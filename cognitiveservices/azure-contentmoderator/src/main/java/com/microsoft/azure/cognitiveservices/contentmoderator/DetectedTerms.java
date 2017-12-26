/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Detected Terms details.
 */
public class DetectedTerms {
    /**
     * Index(Location) of the detected profanity term in the input text
     * content.
     */
    @JsonProperty(value = "Index")
    private Integer index;

    /**
     * Original Index(Location) of the detected profanity term in the input
     * text content.
     */
    @JsonProperty(value = "OriginalIndex")
    private Integer originalIndex;

    /**
     * Matched Terms list Id.
     */
    @JsonProperty(value = "ListId")
    private Integer listId;

    /**
     * Detected profanity term.
     */
    @JsonProperty(value = "Term")
    private String term;

    /**
     * Get the index value.
     *
     * @return the index value
     */
    public Integer index() {
        return this.index;
    }

    /**
     * Set the index value.
     *
     * @param index the index value to set
     * @return the DetectedTerms object itself.
     */
    public DetectedTerms withIndex(Integer index) {
        this.index = index;
        return this;
    }

    /**
     * Get the originalIndex value.
     *
     * @return the originalIndex value
     */
    public Integer originalIndex() {
        return this.originalIndex;
    }

    /**
     * Set the originalIndex value.
     *
     * @param originalIndex the originalIndex value to set
     * @return the DetectedTerms object itself.
     */
    public DetectedTerms withOriginalIndex(Integer originalIndex) {
        this.originalIndex = originalIndex;
        return this;
    }

    /**
     * Get the listId value.
     *
     * @return the listId value
     */
    public Integer listId() {
        return this.listId;
    }

    /**
     * Set the listId value.
     *
     * @param listId the listId value to set
     * @return the DetectedTerms object itself.
     */
    public DetectedTerms withListId(Integer listId) {
        this.listId = listId;
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
     * @return the DetectedTerms object itself.
     */
    public DetectedTerms withTerm(String term) {
        this.term = term;
        return this;
    }

}
