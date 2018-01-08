/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.newssearch.NewsArticle;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.newssearch.SearchResultsAnswer;

/**
 * Defines a news answer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = NewsInner.class)
@JsonTypeName("News")
public class NewsInner extends SearchResultsAnswer {
    /**
     * An array of NewsArticle objects that contain information about news
     * articles that are relevant to the query. If there are no results to
     * return for the request, the array is empty.
     */
    @JsonProperty(value = "value", required = true)
    private List<NewsArticle> value;

    /**
     * Location of local news.
     */
    @JsonProperty(value = "location", access = JsonProperty.Access.WRITE_ONLY)
    private String location;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<NewsArticle> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the NewsInner object itself.
     */
    public NewsInner withValue(List<NewsArticle> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the location value.
     *
     * @return the location value
     */
    public String location() {
        return this.location;
    }

}
