/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Detected Terms details.
 */
public class TermsProperties {
    /**
     * Index(Location) of the detected profanity term in the input text
     * content.
     */
    @JsonProperty(value = "index")
    private Double index;

    /**
     * Original Index(Location) of the detected profanity term in the input
     * text content.
     */
    @JsonProperty(value = "originalIndex")
    private Double originalIndex;

    /**
     * Matched Terms list Id.
     */
    @JsonProperty(value = "listId")
    private Double listId;

    /**
     * Detected profanity term.
     */
    @JsonProperty(value = "term")
    private String term;

    /**
     * Get the index value.
     *
     * @return the index value
     */
    public Double index() {
        return this.index;
    }

    /**
     * Set the index value.
     *
     * @param index the index value to set
     * @return the TermsProperties object itself.
     */
    public TermsProperties withIndex(Double index) {
        this.index = index;
        return this;
    }

    /**
     * Get the originalIndex value.
     *
     * @return the originalIndex value
     */
    public Double originalIndex() {
        return this.originalIndex;
    }

    /**
     * Set the originalIndex value.
     *
     * @param originalIndex the originalIndex value to set
     * @return the TermsProperties object itself.
     */
    public TermsProperties withOriginalIndex(Double originalIndex) {
        this.originalIndex = originalIndex;
        return this;
    }

    /**
     * Get the listId value.
     *
     * @return the listId value
     */
    public Double listId() {
        return this.listId;
    }

    /**
     * Set the listId value.
     *
     * @param listId the listId value to set
     * @return the TermsProperties object itself.
     */
    public TermsProperties withListId(Double listId) {
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
     * @return the TermsProperties object itself.
     */
    public TermsProperties withTerm(String term) {
        this.term = term;
        return this;
    }

}
