// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV1;
import com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2;

/**
 * Generates n-grams of the given size(s). This token filter is implemented
 * using Apache Lucene.
 */
@Fluent
public final class NGramTokenFilter extends TokenFilter {
    private final NGramTokenFilterV1 v1Filter;
    private final NGramTokenFilterV2 v2Filter;

    NGramTokenFilter(NGramTokenFilterV1 v1Filter) {
        super(v1Filter.getName());

        this.v1Filter = v1Filter;
        this.v2Filter = null;
    }

    NGramTokenFilter(NGramTokenFilterV2 v2Filter) {
        super(v2Filter.getName());

        this.v1Filter = null;
        this.v2Filter = v2Filter;
    }

    /**
     * Constructor of {@link NGramTokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public NGramTokenFilter(String name) {
        super(name);

        this.v1Filter = null;
        this.v2Filter = new NGramTokenFilterV2(name);
    }

    /**
     * Get the minGram property: The minimum n-gram length. Default is 1. Must
     * be less than the value of maxGram.
     *
     * @return the minGram value.
     */
    public Integer getMinGram() {
        return (v1Filter != null) ? v1Filter.getMinGram() : v2Filter.getMinGram();
    }

    /**
     * Set the minGram property: The minimum n-gram length. Default is 1. Must
     * be less than the value of maxGram.
     *
     * @param minGram the minGram value to set.
     * @return the NGramTokenFilter object itself.
     */
    public NGramTokenFilter setMinGram(Integer minGram) {
        if (v1Filter != null) {
            v1Filter.setMinGram(minGram);
        } else {
            v2Filter.setMinGram(minGram);
        }
        return this;
    }

    /**
     * Get the maxGram property: The maximum n-gram length. Default is 2.
     *
     * @return the maxGram value.
     */
    public Integer getMaxGram() {
        return (v1Filter != null) ? v1Filter.getMaxGram() : v2Filter.getMaxGram();
    }

    /**
     * Set the maxGram property: The maximum n-gram length. Default is 2.
     *
     * @param maxGram the maxGram value to set.
     * @return the NGramTokenFilter object itself.
     */
    public NGramTokenFilter setMaxGram(Integer maxGram) {
        if (v1Filter != null) {
            v1Filter.setMaxGram(maxGram);
        } else {
            v2Filter.setMaxGram(maxGram);
        }
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return (v1Filter != null) ? v1Filter.toJson(jsonWriter) : v2Filter.toJson(jsonWriter);
    }
}
