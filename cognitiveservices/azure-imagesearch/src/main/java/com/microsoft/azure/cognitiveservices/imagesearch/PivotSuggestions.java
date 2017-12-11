/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the pivot segment.
 */
public class PivotSuggestions {
    /**
     * The segment from the original query to pivot on.
     */
    @JsonProperty(value = "pivot", required = true)
    private String pivot;

    /**
     * A list of suggested queries for the pivot.
     */
    @JsonProperty(value = "suggestions", required = true)
    private List<Query> suggestions;

    /**
     * Get the pivot value.
     *
     * @return the pivot value
     */
    public String pivot() {
        return this.pivot;
    }

    /**
     * Set the pivot value.
     *
     * @param pivot the pivot value to set
     * @return the PivotSuggestions object itself.
     */
    public PivotSuggestions withPivot(String pivot) {
        this.pivot = pivot;
        return this;
    }

    /**
     * Get the suggestions value.
     *
     * @return the suggestions value
     */
    public List<Query> suggestions() {
        return this.suggestions;
    }

    /**
     * Set the suggestions value.
     *
     * @param suggestions the suggestions value to set
     * @return the PivotSuggestions object itself.
     */
    public PivotSuggestions withSuggestions(List<Query> suggestions) {
        this.suggestions = suggestions;
        return this;
    }

}
