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
 * Defines a news answer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = News.class)
@JsonTypeName("News")
public class News extends SearchResultsAnswer {
    /**
     * An array of NewsArticle objects that contain information about news
     * articles that are relevant to the query. If there are no results to
     * return for the request, the array is empty.
     */
    @JsonProperty(value = "value", required = true)
    private List<NewsArticle> value;

    /**
     * The location property.
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
     * @return the News object itself.
     */
    public News withValue(List<NewsArticle> value) {
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
