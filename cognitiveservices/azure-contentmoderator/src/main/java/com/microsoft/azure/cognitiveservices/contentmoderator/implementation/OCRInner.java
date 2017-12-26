/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderator.Status;
import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.KeyValuePair;
import com.microsoft.azure.cognitiveservices.contentmoderator.Candidate;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains the text found in image for the language specified.
 */
public class OCRInner {
    /**
     * The evaluate status.
     */
    @JsonProperty(value = "Status")
    private Status status;

    /**
     * Array of KeyValue.
     */
    @JsonProperty(value = "Metadata")
    private List<KeyValuePair> metadata;

    /**
     * The tracking id.
     */
    @JsonProperty(value = "TrackingId")
    private String trackingId;

    /**
     * The cache id.
     */
    @JsonProperty(value = "CacheId")
    private String cacheId;

    /**
     * The ISO 639-3 code.
     */
    @JsonProperty(value = "Language")
    private String language;

    /**
     * The found text.
     */
    @JsonProperty(value = "Text")
    private String text;

    /**
     * The list of candidate text.
     */
    @JsonProperty(value = "Candidates")
    private List<Candidate> candidates;

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public Status status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the OCRInner object itself.
     */
    public OCRInner withStatus(Status status) {
        this.status = status;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<KeyValuePair> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the OCRInner object itself.
     */
    public OCRInner withMetadata(List<KeyValuePair> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get the trackingId value.
     *
     * @return the trackingId value
     */
    public String trackingId() {
        return this.trackingId;
    }

    /**
     * Set the trackingId value.
     *
     * @param trackingId the trackingId value to set
     * @return the OCRInner object itself.
     */
    public OCRInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

    /**
     * Get the cacheId value.
     *
     * @return the cacheId value
     */
    public String cacheId() {
        return this.cacheId;
    }

    /**
     * Set the cacheId value.
     *
     * @param cacheId the cacheId value to set
     * @return the OCRInner object itself.
     */
    public OCRInner withCacheId(String cacheId) {
        this.cacheId = cacheId;
        return this;
    }

    /**
     * Get the language value.
     *
     * @return the language value
     */
    public String language() {
        return this.language;
    }

    /**
     * Set the language value.
     *
     * @param language the language value to set
     * @return the OCRInner object itself.
     */
    public OCRInner withLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String text() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param text the text value to set
     * @return the OCRInner object itself.
     */
    public OCRInner withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the candidates value.
     *
     * @return the candidates value
     */
    public List<Candidate> candidates() {
        return this.candidates;
    }

    /**
     * Set the candidates value.
     *
     * @param candidates the candidates value to set
     * @return the OCRInner object itself.
     */
    public OCRInner withCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
        return this;
    }

}
