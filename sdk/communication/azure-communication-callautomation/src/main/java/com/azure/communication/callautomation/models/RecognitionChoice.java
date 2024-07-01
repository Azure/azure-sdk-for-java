// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** The RecognitionChoice model. */
public final class RecognitionChoice {
    /*
     * Identifier for a given choice
     */
    @JsonProperty(value = "label", required = true)
    private String label;

    /*
     * List of phrases to recognize
     */
    @JsonProperty(value = "phrases", required = true)
    private List<String> phrases;

    /*
     * The tone property.
     */
    @JsonProperty(value = "tone")
    private DtmfTone tone;

    /**
     * Get the label property: Identifier for a given choice.
     *
     * @return the label value.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Set the label property: Identifier for a given choice.
     *
     * @param label the label value to set.
     * @return the RecognitionChoice object itself.
     */
    public RecognitionChoice setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Get the phrases property: List of phrases to recognize.
     *
     * @return the phrases value.
     */
    public List<String> getPhrases() {
        return this.phrases;
    }

    /**
     * Set the phrases property: The phrases property.
     *
     * @param phrases the phrases value to set.
     * @return the RecognitionChoice object itself.
     */
    public RecognitionChoice setPhrases(List<String> phrases) {
        this.phrases = phrases;
        return this;
    }

    /**
     * Get the tone property: The tone property.
     *
     * @return the tone value.
     */
    public DtmfTone getTone() {
        return this.tone;
    }

    /**
     * Set the tone property: The tone property.
     *
     * @param tone the tone value to set.
     * @return the RecognitionChoice object itself.
     */
    public RecognitionChoice setTone(DtmfTone tone) {
        this.tone = tone;
        return this;
    }
}
