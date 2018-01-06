/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a suggested query string that likely represents the user's intent.
 * The search results include this response if Bing determines that the user
 * may have intended to search for something different. For example, if the
 * user searches for alon brown, Bing may determine that the user likely
 * intended to search for Alton Brown instead (based on past searches by others
 * of Alon Brown).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = SpellSuggestions.class)
@JsonTypeName("SpellSuggestions")
public class SpellSuggestions extends SearchResultsAnswer {
    /**
     * A list of suggested query strings that may represent the user's
     * intention. The list contains only one Query object.
     */
    @JsonProperty(value = "value", required = true)
    private List<Query> value;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<Query> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the SpellSuggestions object itself.
     */
    public SpellSuggestions withValue(List<Query> value) {
        this.value = value;
        return this;
    }

}
