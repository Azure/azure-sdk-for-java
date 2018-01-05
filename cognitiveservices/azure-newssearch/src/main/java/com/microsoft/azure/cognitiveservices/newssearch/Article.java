/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * The Article model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Article.class)
@JsonTypeName("Article")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "NewsArticle", value = NewsArticle.class)
})
public class Article extends CreativeWork {
    /**
     * The number of words in the text of the Article.
     */
    @JsonProperty(value = "wordCount", access = JsonProperty.Access.WRITE_ONLY)
    private Integer wordCount;

    /**
     * Get the wordCount value.
     *
     * @return the wordCount value
     */
    public Integer wordCount() {
        return this.wordCount;
    }

}
