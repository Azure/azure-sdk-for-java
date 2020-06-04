// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generates n-grams of the given size(s) starting from the front or the back
 * of an input token. This token filter is implemented using Apache Lucene.
 */
public final class EdgeNGramTokenFilter extends TokenFilter {
    private static final String V2_ODATA_TYPE = "#Microsoft.Azure.Search.EdgeNGramTokenFilterV2";

    @JsonProperty(value = "@odata.type")
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

    /*
     * Specifies which side of the input the n-gram should be generated from.
     * Default is "front". Possible values include: 'Front', 'Back'
     */
    @JsonProperty(value = "side")
    private EdgeNGramTokenFilterSide side;

    /**
     * Constructor for {@link EdgeNGramTokenFilter}.
     */
    public EdgeNGramTokenFilter() {
        odataType = V2_ODATA_TYPE;
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
     * @return the EdgeNGramTokenFilter object itself.
     */
    public EdgeNGramTokenFilter setMinGram(Integer minGram) {
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
     * @return the EdgeNGramTokenFilter object itself.
     */
    public EdgeNGramTokenFilter setMaxGram(Integer maxGram) {
        this.maxGram = maxGram;
        return this;
    }

    /**
     * Get the side property: Specifies which side of the input the n-gram
     * should be generated from. Default is "front". Possible values include:
     * 'Front', 'Back'.
     *
     * @return the side value.
     */
    public EdgeNGramTokenFilterSide getSide() {
        return this.side;
    }

    /**
     * Set the side property: Specifies which side of the input the n-gram
     * should be generated from. Default is "front". Possible values include:
     * 'Front', 'Back'.
     *
     * @param side the side value to set.
     * @return the EdgeNGramTokenFilter object itself.
     */
    public EdgeNGramTokenFilter setSide(EdgeNGramTokenFilterSide side) {
        this.side = side;
        return this;
    }
}
