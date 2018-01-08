/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The NewsTopic model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = NewsTopic.class)
@JsonTypeName("News/Topic")
public class NewsTopic extends Thing {
    /**
     * A Boolean value that indicates whether the topic is considered breaking
     * news. If the topic is considered breaking news, the value is true.
     */
    @JsonProperty(value = "isBreakingNews", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean isBreakingNews;

    /**
     * A search query term that returns this trending topic.
     */
    @JsonProperty(value = "query", access = JsonProperty.Access.WRITE_ONLY)
    private Query query;

    /**
     * The URL to the Bing News search results for the search query term.
     */
    @JsonProperty(value = "newsSearchUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String newsSearchUrl;

    /**
     * Get the isBreakingNews value.
     *
     * @return the isBreakingNews value
     */
    public Boolean isBreakingNews() {
        return this.isBreakingNews;
    }

    /**
     * Get the query value.
     *
     * @return the query value
     */
    public Query query() {
        return this.query;
    }

    /**
     * Get the newsSearchUrl value.
     *
     * @return the newsSearchUrl value
     */
    public String newsSearchUrl() {
        return this.newsSearchUrl;
    }

}
