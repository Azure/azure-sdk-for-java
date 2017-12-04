/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.spellcheck;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The SpellingTokenSuggestion model.
 */
public class SpellingTokenSuggestion {
    /**
     * The suggestion property.
     */
    @JsonProperty(value = "suggestion", required = true)
    private String suggestion;

    /**
     * The score property.
     */
    @JsonProperty(value = "score", access = JsonProperty.Access.WRITE_ONLY)
    private Double score;

    /**
     * The pingUrlSuffix property.
     */
    @JsonProperty(value = "pingUrlSuffix", access = JsonProperty.Access.WRITE_ONLY)
    private String pingUrlSuffix;

    /**
     * Get the suggestion value.
     *
     * @return the suggestion value
     */
    public String suggestion() {
        return this.suggestion;
    }

    /**
     * Set the suggestion value.
     *
     * @param suggestion the suggestion value to set
     * @return the SpellingTokenSuggestion object itself.
     */
    public SpellingTokenSuggestion withSuggestion(String suggestion) {
        this.suggestion = suggestion;
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
     * Get the pingUrlSuffix value.
     *
     * @return the pingUrlSuffix value
     */
    public String pingUrlSuffix() {
        return this.pingUrlSuffix;
    }

}
