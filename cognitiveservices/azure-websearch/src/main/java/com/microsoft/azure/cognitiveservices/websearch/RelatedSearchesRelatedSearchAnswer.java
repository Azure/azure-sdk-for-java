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
 * Defines a list of related queries made by others.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = RelatedSearchesRelatedSearchAnswer.class)
@JsonTypeName("RelatedSearches/RelatedSearchAnswer")
public class RelatedSearchesRelatedSearchAnswer extends SearchResultsAnswer {
    /**
     * A list of related queries that were made by others.
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
     * @return the RelatedSearchesRelatedSearchAnswer object itself.
     */
    public RelatedSearchesRelatedSearchAnswer withValue(List<Query> value) {
        this.value = value;
        return this;
    }

}
