/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The DetectedLanguage model.
 */
public class DetectedLanguage {
    /**
     * Long name of a detected language (e.g. English, French).
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * A two letter representation of the detected language according to the
     * ISO 639-1 standard (e.g. en, fr).
     */
    @JsonProperty(value = "iso6391Name")
    private String iso6391Name;

    /**
     * A confidence score between 0 and 1. Scores close to 1 indicate 100%
     * certainty that the identified language is true.
     */
    @JsonProperty(value = "score")
    private Double score;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the DetectedLanguage object itself.
     */
    public DetectedLanguage withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the iso6391Name value.
     *
     * @return the iso6391Name value
     */
    public String iso6391Name() {
        return this.iso6391Name;
    }

    /**
     * Set the iso6391Name value.
     *
     * @param iso6391Name the iso6391Name value to set
     * @return the DetectedLanguage object itself.
     */
    public DetectedLanguage withIso6391Name(String iso6391Name) {
        this.iso6391Name = iso6391Name;
        return this;
    }

    /**
     * Get the score value.
     *
     * @return the score value
     */
    public Double score() {
        return this.score;
    }

    /**
     * Set the score value.
     *
     * @param score the score value to set
     * @return the DetectedLanguage object itself.
     */
    public DetectedLanguage withScore(Double score) {
        this.score = score;
        return this;
    }

}
