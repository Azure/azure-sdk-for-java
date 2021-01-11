// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.search.documents.implementation.converters.NGramTokenFilterHelper;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generates n-grams of the given size(s). This token filter is implemented
 * using Apache Lucene.
 */
@Fluent
public final class NGramTokenFilter extends TokenFilter {
    private String odataType;

    /*
     * The minimum n-gram length. Default is 1. Must be less than the value of
     * maxGram.
     */
    @JsonProperty(value = "minGram")
    private Integer minGram;

    /*
     * The maximum n-gram length. Default is 2.
     */
    @JsonProperty(value = "maxGram")
    private Integer maxGram;

    static {
        NGramTokenFilterHelper.setAccessor(new NGramTokenFilterHelper.NGramTokenFilterAccessor() {
            @Override
            public void setODataType(NGramTokenFilter tokenFilter, String odataType) {
                tokenFilter.setODataType(odataType);
            }

            @Override
            public String getODataType(NGramTokenFilter tokenFilter) {
                return tokenFilter.getODataType();
            }
        });
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
        odataType = "#Microsoft.Azure.Search.KeywordTokenizerV2";
    }

    /**
     * Get the minGram property: The minimum n-gram length. Default is 1. Must
     * be less than the value of maxGram.
     *
     * @return the minGram value.
     */
    public Integer getMinGram() {
        return this.minGram;
    }

    /**
     * Set the minGram property: The minimum n-gram length. Default is 1. Must
     * be less than the value of maxGram.
     *
     * @param minGram the minGram value to set.
     * @return the NGramTokenFilter object itself.
     */
    public NGramTokenFilter setMinGram(Integer minGram) {
        this.minGram = minGram;
        return this;
    }

    /**
     * Get the maxGram property: The maximum n-gram length. Default is 2.
     *
     * @return the maxGram value.
     */
    public Integer getMaxGram() {
        return this.maxGram;
    }

    /**
     * Set the maxGram property: The maximum n-gram length. Default is 2.
     *
     * @param maxGram the maxGram value to set.
     * @return the NGramTokenFilter object itself.
     */
    public NGramTokenFilter setMaxGram(Integer maxGram) {
        this.maxGram = maxGram;
        return this;
    }

    /**
     * The private setter to set the odataType property
     * via {@link NGramTokenFilterHelper.NGramTokenFilterAccessor}.
     *
     * @param odataType The OData type.
     */
    private void setODataType(String odataType) {
        this.odataType = odataType;
    }

    /**
     * The private getter to get the odataType property
     * via {@link NGramTokenFilterHelper.NGramTokenFilterAccessor}.
     *
     * @return The OData type.
     */
    private String getODataType() {
        return this.odataType;
    }
}
