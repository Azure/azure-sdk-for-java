/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * The SearchResultsAnswer model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = SearchResultsAnswer.class)
@JsonTypeName("SearchResultsAnswer")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "Web/WebAnswer", value = WebWebAnswer.class),
    @JsonSubTypes.Type(name = "Images", value = Images.class),
    @JsonSubTypes.Type(name = "News", value = News.class),
    @JsonSubTypes.Type(name = "RelatedSearches/RelatedSearchAnswer", value = RelatedSearchesRelatedSearchAnswer.class),
    @JsonSubTypes.Type(name = "SpellSuggestions", value = SpellSuggestions.class),
    @JsonSubTypes.Type(name = "TimeZone", value = TimeZone.class),
    @JsonSubTypes.Type(name = "Videos", value = Videos.class),
    @JsonSubTypes.Type(name = "Places", value = Places.class)
})
public class SearchResultsAnswer extends Answer {
    /**
     * The queryContext property.
     */
    @JsonProperty(value = "queryContext", access = JsonProperty.Access.WRITE_ONLY)
    private QueryContext queryContext;

    /**
     * The estimated number of webpages that are relevant to the query. Use
     * this number along with the count and offset query parameters to page the
     * results.
     */
    @JsonProperty(value = "totalEstimatedMatches", access = JsonProperty.Access.WRITE_ONLY)
    private Long totalEstimatedMatches;

    /**
     * The isFamilyFriendly property.
     */
    @JsonProperty(value = "isFamilyFriendly", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean isFamilyFriendly;

    /**
     * Get the queryContext value.
     *
     * @return the queryContext value
     */
    public QueryContext queryContext() {
        return this.queryContext;
    }

    /**
     * Get the totalEstimatedMatches value.
     *
     * @return the totalEstimatedMatches value
     */
    public Long totalEstimatedMatches() {
        return this.totalEstimatedMatches;
    }

    /**
     * Get the isFamilyFriendly value.
     *
     * @return the isFamilyFriendly value
     */
    public Boolean isFamilyFriendly() {
        return this.isFamilyFriendly;
    }

}
