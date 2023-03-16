// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Creates combinations of tokens as a single token. This token filter is
 * implemented using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.ShingleTokenFilter")
@Fluent
public final class ShingleTokenFilter extends TokenFilter {
    /*
     * The maximum shingle size. Default and minimum value is 2.
     */
    @JsonProperty(value = "maxShingleSize")
    private Integer maxShingleSize;

    /*
     * The minimum shingle size. Default and minimum value is 2. Must be less
     * than the value of maxShingleSize.
     */
    @JsonProperty(value = "minShingleSize")
    private Integer minShingleSize;

    /*
     * A value indicating whether the output stream will contain the input
     * tokens (unigrams) as well as shingles. Default is true.
     */
    @JsonProperty(value = "outputUnigrams")
    private Boolean outputUnigrams;

    /*
     * A value indicating whether to output unigrams for those times when no
     * shingles are available. This property takes precedence when
     * outputUnigrams is set to false. Default is false.
     */
    @JsonProperty(value = "outputUnigramsIfNoShingles")
    private Boolean outputUnigramsIfNoShingles;

    /*
     * The string to use when joining adjacent tokens to form a shingle.
     * Default is a single space (" ").
     */
    @JsonProperty(value = "tokenSeparator")
    private String tokenSeparator;

    /*
     * The string to insert for each position at which there is no token.
     * Default is an underscore ("_").
     */
    @JsonProperty(value = "filterToken")
    private String filterToken;

    /**
     * Constructor of {@link ShingleTokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public ShingleTokenFilter(String name) {
        super(name);
    }

    /**
     * Get the maxShingleSize property: The maximum shingle size. Default and
     * minimum value is 2.
     *
     * @return the maxShingleSize value.
     */
    public Integer getMaxShingleSize() {
        return this.maxShingleSize;
    }

    /**
     * Set the maxShingleSize property: The maximum shingle size. Default and
     * minimum value is 2.
     *
     * @param maxShingleSize the maxShingleSize value to set.
     * @return the ShingleTokenFilter object itself.
     */
    public ShingleTokenFilter setMaxShingleSize(Integer maxShingleSize) {
        this.maxShingleSize = maxShingleSize;
        return this;
    }

    /**
     * Get the minShingleSize property: The minimum shingle size. Default and
     * minimum value is 2. Must be less than the value of maxShingleSize.
     *
     * @return the minShingleSize value.
     */
    public Integer getMinShingleSize() {
        return this.minShingleSize;
    }

    /**
     * Set the minShingleSize property: The minimum shingle size. Default and
     * minimum value is 2. Must be less than the value of maxShingleSize.
     *
     * @param minShingleSize the minShingleSize value to set.
     * @return the ShingleTokenFilter object itself.
     */
    public ShingleTokenFilter setMinShingleSize(Integer minShingleSize) {
        this.minShingleSize = minShingleSize;
        return this;
    }

    /**
     * Get the outputUnigrams property: A value indicating whether the output
     * stream will contain the input tokens (unigrams) as well as shingles.
     * Default is true.
     *
     * @return the outputUnigrams value.
     */
    public Boolean areOutputUnigrams() {
        return this.outputUnigrams;
    }

    /**
     * Set the outputUnigrams property: A value indicating whether the output
     * stream will contain the input tokens (unigrams) as well as shingles.
     * Default is true.
     *
     * @param outputUnigrams the outputUnigrams value to set.
     * @return the ShingleTokenFilter object itself.
     */
    public ShingleTokenFilter setOutputUnigrams(Boolean outputUnigrams) {
        this.outputUnigrams = outputUnigrams;
        return this;
    }

    /**
     * Get the outputUnigramsIfNoShingles property: A value indicating whether
     * to output unigrams for those times when no shingles are available. This
     * property takes precedence when outputUnigrams is set to false. Default
     * is false.
     *
     * @return the outputUnigramsIfNoShingles value.
     */
    public Boolean areOutputUnigramsIfNoShingles() {
        return this.outputUnigramsIfNoShingles;
    }

    /**
     * Set the outputUnigramsIfNoShingles property: A value indicating whether
     * to output unigrams for those times when no shingles are available. This
     * property takes precedence when outputUnigrams is set to false. Default
     * is false.
     *
     * @param outputUnigramsIfNoShingles the outputUnigramsIfNoShingles value
     * to set.
     * @return the ShingleTokenFilter object itself.
     */
    public ShingleTokenFilter setOutputUnigramsIfNoShingles(Boolean outputUnigramsIfNoShingles) {
        this.outputUnigramsIfNoShingles = outputUnigramsIfNoShingles;
        return this;
    }

    /**
     * Get the tokenSeparator property: The string to use when joining adjacent
     * tokens to form a shingle. Default is a single space (" ").
     *
     * @return the tokenSeparator value.
     */
    public String getTokenSeparator() {
        return this.tokenSeparator;
    }

    /**
     * Set the tokenSeparator property: The string to use when joining adjacent
     * tokens to form a shingle. Default is a single space (" ").
     *
     * @param tokenSeparator the tokenSeparator value to set.
     * @return the ShingleTokenFilter object itself.
     */
    public ShingleTokenFilter setTokenSeparator(String tokenSeparator) {
        this.tokenSeparator = tokenSeparator;
        return this;
    }

    /**
     * Get the filterToken property: The string to insert for each position at
     * which there is no token. Default is an underscore ("_").
     *
     * @return the filterToken value.
     */
    public String getFilterToken() {
        return this.filterToken;
    }

    /**
     * Set the filterToken property: The string to insert for each position at
     * which there is no token. Default is an underscore ("_").
     *
     * @param filterToken the filterToken value to set.
     * @return the ShingleTokenFilter object itself.
     */
    public ShingleTokenFilter setFilterToken(String filterToken) {
        this.filterToken = filterToken;
        return this;
    }
}
