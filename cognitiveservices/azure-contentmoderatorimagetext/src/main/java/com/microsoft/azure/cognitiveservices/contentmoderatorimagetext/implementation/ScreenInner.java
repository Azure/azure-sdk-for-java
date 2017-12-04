/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ResponseStatus;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.PIIDetailsProperties;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.TermsProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response for a Screen text request.
 */
public class ScreenInner {
    /**
     * The original text.
     */
    @JsonProperty(value = "originalText")
    private String originalText;

    /**
     * The normalized text.
     */
    @JsonProperty(value = "normalizedText")
    private String normalizedText;

    /**
     * The misrepresentation text.
     */
    @JsonProperty(value = "misrepresentation")
    private List<String> misrepresentation;

    /**
     * The evaluate status.
     */
    @JsonProperty(value = "status")
    private ResponseStatus status;

    /**
     * Personal Identifier Information details.
     */
    @JsonProperty(value = "pii")
    private PIIDetailsProperties pii;

    /**
     * Language of the input text content.
     */
    @JsonProperty(value = "language")
    private String language;

    /**
     * The terms property.
     */
    @JsonProperty(value = "terms")
    private List<TermsProperties> terms;

    /**
     * Unique Content Moderator transaction Id.
     */
    @JsonProperty(value = "trackingId")
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
     * Get the status value.
     *
     * @return the status value
     */
    public ResponseStatus status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withStatus(ResponseStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get the pii value.
     *
     * @return the pii value
     */
    public PIIDetailsProperties pii() {
        return this.pii;
    }

    /**
     * Set the pii value.
     *
     * @param pii the pii value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withPii(PIIDetailsProperties pii) {
        this.pii = pii;
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
    public List<TermsProperties> terms() {
        return this.terms;
    }

    /**
     * Set the terms value.
     *
     * @param terms the terms value to set
     * @return the ScreenInner object itself.
     */
    public ScreenInner withTerms(List<TermsProperties> terms) {
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
