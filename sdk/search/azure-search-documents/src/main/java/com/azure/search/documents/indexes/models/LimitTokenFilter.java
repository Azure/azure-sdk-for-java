// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Limits the number of tokens while indexing. This token filter is implemented
 * using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.LimitTokenFilter")
@Fluent
public final class LimitTokenFilter extends TokenFilter {
    /*
     * The maximum number of tokens to produce. Default is 1.
     */
    @JsonProperty(value = "maxTokenCount")
    private Integer maxTokenCount;

    /*
     * A value indicating whether all tokens from the input must be consumed
     * even if maxTokenCount is reached. Default is false.
     */
    @JsonProperty(value = "consumeAllTokens")
    private Boolean allTokensConsumed;

    /**
     * Constructor of {@link TokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public LimitTokenFilter(String name) {
        super(name);
    }

    /**
     * Get the maxTokenCount property: The maximum number of tokens to produce.
     * Default is 1.
     *
     * @return the maxTokenCount value.
     */
    public Integer getMaxTokenCount() {
        return this.maxTokenCount;
    }

    /**
     * Set the maxTokenCount property: The maximum number of tokens to produce.
     * Default is 1.
     *
     * @param maxTokenCount the maxTokenCount value to set.
     * @return the LimitTokenFilter object itself.
     */
    public LimitTokenFilter setMaxTokenCount(Integer maxTokenCount) {
        this.maxTokenCount = maxTokenCount;
        return this;
    }

    /**
     * Get the consumeAllTokens property: A value indicating whether all tokens
     * from the input must be consumed even if maxTokenCount is reached.
     * Default is false.
     *
     * @return the consumeAllTokens value.
     */
    public Boolean areAllTokensConsumed() {
        return this.allTokensConsumed;
    }

    /**
     * Set the consumeAllTokens property: A value indicating whether all tokens
     * from the input must be consumed even if maxTokenCount is reached.
     * Default is false.
     *
     * @param allTokensConsumed the consumeAllTokens value to set.
     * @return the LimitTokenFilter object itself.
     */
    public LimitTokenFilter setAllTokensConsumed(Boolean allTokensConsumed) {
        this.allTokensConsumed = allTokensConsumed;
        return this;
    }
}
