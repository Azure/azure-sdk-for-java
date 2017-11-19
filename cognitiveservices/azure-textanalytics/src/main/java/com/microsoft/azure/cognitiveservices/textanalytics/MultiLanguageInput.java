/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The MultiLanguageInput model.
 */
public class MultiLanguageInput {
    /**
     * This is the 2 letter ISO 639-1 representation of a language. For
     * example, use "en" for English; "es" for Spanish etc.,.
     */
    @JsonProperty(value = "language")
    private String language;

    /**
     * Unique, non-empty document identifier.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * The text property.
     */
    @JsonProperty(value = "text")
    private String text;

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
     * @return the MultiLanguageInput object itself.
     */
    public MultiLanguageInput withLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the MultiLanguageInput object itself.
     */
    public MultiLanguageInput withId(String id) {
        this.id = id;
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
     * @return the MultiLanguageInput object itself.
     */
    public MultiLanguageInput withText(String text) {
        this.text = text;
        return this;
    }

}
