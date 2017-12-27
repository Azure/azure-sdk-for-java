/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.Classification;
import com.microsoft.azure.cognitiveservices.contentmoderator.Status;
import com.microsoft.azure.cognitiveservices.contentmoderator.PII;
import com.microsoft.azure.cognitiveservices.contentmoderator.DetectedTerms;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response for a Screen text request.
 */
public class ScreenInner {
    /**
     * The original text.
     */
    @JsonProperty(value = "OriginalText")
    private String originalText;

    /**
     * The normalized text.
     */
    @JsonProperty(value = "NormalizedText")
    private String normalizedText;

    /**
     * The autocorrected text.
     */
    @JsonProperty(value = "AutoCorrectedText")
    private String autoCorrectedText;

    /**
     * The misrepresentation text.
     */
    @JsonProperty(value = "Misrepresentation")
    private List<String> misrepresentation;

    /**
     * The classification details of the text.
     */
    @JsonProperty(value = "Classification")
    private Classification classification;

    /**
     * The evaluate status.
     */
    @JsonProperty(value = "Status")
    private Status status;

    /**
     * Personal Identifier Information details.
     */
    @JsonProperty(value = "PII")
    private PII pII;

    /**
     * Language of the input text content.
     */
    @JsonProperty(value = "Language")
    private String language;

    /**
     * The terms property.
     */
    @JsonProperty(value = "Terms")
    private List<DetectedTerms> terms;

    /**
     * Unique Content Moderator transaction Id.
     */
    @JsonProperty(value = "TrackingId")
    private String trackingId;

    /**
     * Get the originalText value.
     *
     * @return the originalText value
     */
    public String originalText() {
        return this.originalText;
    }

    /**
     * Set the originalText value.
     *
     * @param originalText the originalText value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withOriginalText(String originalText) {
        this.originalText = originalText;
        return this;
    }

    /**
     * Get the normalizedText value.
     *
     * @return the normalizedText value
     */
    public String normalizedText() {
        return this.normalizedText;
    }

    /**
     * Set the normalizedText value.
     *
     * @param normalizedText the normalizedText value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withNormalizedText(String normalizedText) {
        this.normalizedText = normalizedText;
        return this;
    }

    /**
     * Get the autoCorrectedText value.
     *
     * @return the autoCorrectedText value
     */
    public String autoCorrectedText() {
        return this.autoCorrectedText;
    }

    /**
     * Set the autoCorrectedText value.
     *
     * @param autoCorrectedText the autoCorrectedText value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withAutoCorrectedText(String autoCorrectedText) {
        this.autoCorrectedText = autoCorrectedText;
        return this;
    }

    /**
     * Get the misrepresentation value.
     *
     * @return the misrepresentation value
     */
    public List<String> misrepresentation() {
        return this.misrepresentation;
    }

    /**
     * Set the misrepresentation value.
     *
     * @param misrepresentation the misrepresentation value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withMisrepresentation(List<String> misrepresentation) {
        this.misrepresentation = misrepresentation;
        return this;
    }

    /**
     * Get the classification value.
     *
     * @return the classification value
     */
    public Classification classification() {
        return this.classification;
    }

    /**
     * Set the classification value.
     *
     * @param classification the classification value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withClassification(Classification classification) {
        this.classification = classification;
        return this;
    }

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
     * @return the ScreenInner object itself.
     */
    public ScreenInner withStatus(Status status) {
        this.status = status;
        return this;
    }

    /**
     * Get the pII value.
     *
     * @return the pII value
     */
    public PII pII() {
        return this.pII;
    }

    /**
     * Set the pII value.
     *
     * @param pII the pII value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withPII(PII pII) {
        this.pII = pII;
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
     * @return the ScreenInner object itself.
     */
    public ScreenInner withLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Get the terms value.
     *
     * @return the terms value
     */
    public List<DetectedTerms> terms() {
        return this.terms;
    }

    /**
     * Set the terms value.
     *
     * @param terms the terms value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withTerms(List<DetectedTerms> terms) {
        this.terms = terms;
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
     * @return the ScreenInner object itself.
     */
    public ScreenInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

}
