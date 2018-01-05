/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.newssearch.NewsTopic;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.newssearch.Answer;

/**
 * The TrendingTopicsInner model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = TrendingTopicsInner.class)
@JsonTypeName("TrendingTopics")
public class TrendingTopicsInner extends Answer {
    /**
     * A list of trending news topics on Bing.
     */
    @JsonProperty(value = "value", required = true)
    private List<NewsTopic> value;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<NewsTopic> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the TrendingTopicsInner object itself.
     */
    public TrendingTopicsInner withValue(List<NewsTopic> value) {
        this.value = value;
        return this;
    }

}
