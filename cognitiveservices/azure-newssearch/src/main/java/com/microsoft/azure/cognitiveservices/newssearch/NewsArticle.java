/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a news article.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = NewsArticle.class)
@JsonTypeName("NewsArticle")
public class NewsArticle extends Article {
    /**
     * The news category that the article belongs to. For example, Sports. If
     * the news category cannot be determined, the article does not include
     * this field.
     */
    @JsonProperty(value = "category", access = JsonProperty.Access.WRITE_ONLY)
    private String category;

    /**
     * A Boolean value that indicates whether the news article is a headline.
     * If true, the article is a headline. The article includes this field only
     * for news categories requests that do not specify the category query
     * parameter.
     */
    @JsonProperty(value = "headline", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean headline;

    /**
     * A list of related news articles.
     */
    @JsonProperty(value = "clusteredArticles", access = JsonProperty.Access.WRITE_ONLY)
    private List<NewsArticle> clusteredArticles;

    /**
     * Get the category value.
     *
     * @return the category value
     */
    public String category() {
        return this.category;
    }

    /**
     * Get the headline value.
     *
     * @return the headline value
     */
    public Boolean headline() {
        return this.headline;
    }

    /**
     * Get the clusteredArticles value.
     *
     * @return the clusteredArticles value
     */
    public List<NewsArticle> clusteredArticles() {
        return this.clusteredArticles;
    }

}
