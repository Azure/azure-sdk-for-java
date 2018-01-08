/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch.implementation;

import com.microsoft.azure.cognitiveservices.websearch.QueryContext;
import com.microsoft.azure.cognitiveservices.websearch.WebWebAnswer;
import com.microsoft.azure.cognitiveservices.websearch.Images;
import com.microsoft.azure.cognitiveservices.websearch.News;
import com.microsoft.azure.cognitiveservices.websearch.RelatedSearchesRelatedSearchAnswer;
import com.microsoft.azure.cognitiveservices.websearch.SpellSuggestions;
import com.microsoft.azure.cognitiveservices.websearch.TimeZone;
import com.microsoft.azure.cognitiveservices.websearch.Videos;
import com.microsoft.azure.cognitiveservices.websearch.Computation;
import com.microsoft.azure.cognitiveservices.websearch.RankingRankingResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.websearch.Response;

/**
 * Defines the top-level object that the response includes when the request
 * succeeds.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = SearchResponseInner.class)
@JsonTypeName("SearchResponse")
public class SearchResponseInner extends Response {
    /**
     * An object that contains the query string that Bing used for the request.
     * This object contains the query string as entered by the user. It may
     * also contain an altered query string that Bing used for the query if the
     * query string contained a spelling mistake.
     */
    @JsonProperty(value = "queryContext", access = JsonProperty.Access.WRITE_ONLY)
    private QueryContext queryContext;

    /**
     * A list of webpages that are relevant to the search query.
     */
    @JsonProperty(value = "webPages", access = JsonProperty.Access.WRITE_ONLY)
    private WebWebAnswer webPages;

    /**
     * A list of images that are relevant to the search query.
     */
    @JsonProperty(value = "images", access = JsonProperty.Access.WRITE_ONLY)
    private Images images;

    /**
     * A list of news articles that are relevant to the search query.
     */
    @JsonProperty(value = "news", access = JsonProperty.Access.WRITE_ONLY)
    private News news;

    /**
     * A list of related queries made by others.
     */
    @JsonProperty(value = "relatedSearches", access = JsonProperty.Access.WRITE_ONLY)
    private RelatedSearchesRelatedSearchAnswer relatedSearches;

    /**
     * The query string that likely represents the user's intent.
     */
    @JsonProperty(value = "spellSuggestions", access = JsonProperty.Access.WRITE_ONLY)
    private SpellSuggestions spellSuggestions;

    /**
     * The date and time of one or more geographic locations.
     */
    @JsonProperty(value = "timeZone", access = JsonProperty.Access.WRITE_ONLY)
    private TimeZone timeZone;

    /**
     * A list of videos that are relevant to the search query.
     */
    @JsonProperty(value = "videos", access = JsonProperty.Access.WRITE_ONLY)
    private Videos videos;

    /**
     * The answer to a math expression or units conversion expression.
     */
    @JsonProperty(value = "computation", access = JsonProperty.Access.WRITE_ONLY)
    private Computation computation;

    /**
     * The order that Bing suggests that you display the search results in.
     */
    @JsonProperty(value = "rankingResponse", access = JsonProperty.Access.WRITE_ONLY)
    private RankingRankingResponse rankingResponse;

    /**
     * Get the queryContext value.
     *
     * @return the queryContext value
     */
    public QueryContext queryContext() {
        return this.queryContext;
    }

    /**
     * Get the webPages value.
     *
     * @return the webPages value
     */
    public WebWebAnswer webPages() {
        return this.webPages;
    }

    /**
     * Get the images value.
     *
     * @return the images value
     */
    public Images images() {
        return this.images;
    }

    /**
     * Get the news value.
     *
     * @return the news value
     */
    public News news() {
        return this.news;
    }

    /**
     * Get the relatedSearches value.
     *
     * @return the relatedSearches value
     */
    public RelatedSearchesRelatedSearchAnswer relatedSearches() {
        return this.relatedSearches;
    }

    /**
     * Get the spellSuggestions value.
     *
     * @return the spellSuggestions value
     */
    public SpellSuggestions spellSuggestions() {
        return this.spellSuggestions;
    }

    /**
     * Get the timeZone value.
     *
     * @return the timeZone value
     */
    public TimeZone timeZone() {
        return this.timeZone;
    }

    /**
     * Get the videos value.
     *
     * @return the videos value
     */
    public Videos videos() {
        return this.videos;
    }

    /**
     * Get the computation value.
     *
     * @return the computation value
     */
    public Computation computation() {
        return this.computation;
    }

    /**
     * Get the rankingResponse value.
     *
     * @return the rankingResponse value
     */
    public RankingRankingResponse rankingResponse() {
        return this.rankingResponse;
    }

}
