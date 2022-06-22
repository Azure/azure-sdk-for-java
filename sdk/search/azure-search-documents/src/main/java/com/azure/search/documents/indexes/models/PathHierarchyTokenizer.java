// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;

/**
 * Tokenizer for path-like hierarchies. This tokenizer is implemented using
 * Apache Lucene.
 */
@Fluent
public final class PathHierarchyTokenizer extends LexicalTokenizer {
    /*
     * The delimiter character to use. Default is "/".
     */
    private Character delimiter;

    /*
     * A value that, if set, replaces the delimiter character. Default is "/".
     */
    private Character replacement;

    /*
     * The maximum token length. Default and maximum is 300.
     */
    private Integer maxTokenLength;

    /*
     * A value indicating whether to generate tokens in reverse order. Default
     * is false.
     */
    private Boolean tokenOrderReversed;

    /*
     * The number of initial tokens to skip. Default is 0.
     */
    private Integer numberOfTokensToSkip;

    /**
     * Constructor of {@link PathHierarchyTokenizer}.
     *
     * @param name The name of the tokenizer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public PathHierarchyTokenizer(String name) {
        super(name);
    }

    /**
     * Get the delimiter property: The delimiter character to use. Default is
     * "/".
     *
     * @return the delimiter value.
     */
    public Character getDelimiter() {
        return this.delimiter;
    }

    /**
     * Set the delimiter property: The delimiter character to use. Default is
     * "/".
     *
     * @param delimiter the delimiter value to set.
     * @return the PathHierarchyTokenizerV2 object itself.
     */
    public PathHierarchyTokenizer setDelimiter(Character delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Get the replacement property: A value that, if set, replaces the
     * delimiter character. Default is "/".
     *
     * @return the replacement value.
     */
    public Character getReplacement() {
        return this.replacement;
    }

    /**
     * Set the replacement property: A value that, if set, replaces the
     * delimiter character. Default is "/".
     *
     * @param replacement the replacement value to set.
     * @return the PathHierarchyTokenizerV2 object itself.
     */
    public PathHierarchyTokenizer setReplacement(Character replacement) {
        this.replacement = replacement;
        return this;
    }

    /**
     * Get the maxTokenLength property: The maximum token length. Default and
     * maximum is 300.
     *
     * @return the maxTokenLength value.
     */
    public Integer getMaxTokenLength() {
        return this.maxTokenLength;
    }

    /**
     * Set the maxTokenLength property: The maximum token length. Default and
     * maximum is 300.
     *
     * @param maxTokenLength the maxTokenLength value to set.
     * @return the PathHierarchyTokenizerV2 object itself.
     */
    public PathHierarchyTokenizer setMaxTokenLength(Integer maxTokenLength) {
        this.maxTokenLength = maxTokenLength;
        return this;
    }

    /**
     * Get the reverseTokenOrder property: A value indicating whether to
     * generate tokens in reverse order. Default is false.
     *
     * @return the reverseTokenOrder value.
     */
    public Boolean isTokenOrderReversed() {
        return this.tokenOrderReversed;
    }

    /**
     * Set the reverseTokenOrder property: A value indicating whether to
     * generate tokens in reverse order. Default is false.
     *
     * @param tokenOrderReversed the reverseTokenOrder value to set.
     * @return the PathHierarchyTokenizerV2 object itself.
     */
    public PathHierarchyTokenizer setTokenOrderReversed(Boolean tokenOrderReversed) {
        this.tokenOrderReversed = tokenOrderReversed;
        return this;
    }

    /**
     * Get the numberOfTokensToSkip property: The number of initial tokens to
     * skip. Default is 0.
     *
     * @return the numberOfTokensToSkip value.
     */
    public Integer getNumberOfTokensToSkip() {
        return this.numberOfTokensToSkip;
    }

    /**
     * Set the numberOfTokensToSkip property: The number of initial tokens to
     * skip. Default is 0.
     *
     * @param numberOfTokensToSkip the numberOfTokensToSkip value to set.
     * @return the PathHierarchyTokenizerV2 object itself.
     */
    public PathHierarchyTokenizer setNumberOfTokensToSkip(Integer numberOfTokensToSkip) {
        this.numberOfTokensToSkip = numberOfTokensToSkip;
        return this;
    }
}
