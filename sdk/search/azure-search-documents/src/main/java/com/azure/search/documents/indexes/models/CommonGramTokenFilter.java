// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Construct bigrams for frequently occurring terms while indexing. Single
 * terms are still indexed too, with bigrams overlaid. This token filter is
 * implemented using Apache Lucene.
 */
@Fluent
public final class CommonGramTokenFilter extends TokenFilter {
    /*
     * The set of common words.
     */
    private final List<String> commonWords;

    /*
     * A value indicating whether common words matching will be case
     * insensitive. Default is false.
     */
    private Boolean caseIgnored;

    /*
     * A value that indicates whether the token filter is in query mode. When
     * in query mode, the token filter generates bigrams and then removes
     * common words and single terms followed by a common word. Default is
     * false.
     */
    private Boolean queryModeUsed;

    /**
     * Constructor of {@link TokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     * @param commonWords The set of common words.
     */
    public CommonGramTokenFilter(String name, List<String> commonWords) {
        super(name);
        this.commonWords = commonWords;
    }

    /**
     * Get the commonWords property: The set of common words.
     *
     * @return the commonWords value.
     */
    public List<String> getCommonWords() {
        return this.commonWords;
    }

    /**
     * Get the ignoreCase property: A value indicating whether common words
     * matching will be case insensitive. Default is false.
     *
     * @return the ignoreCase value.
     */
    public Boolean isCaseIgnored() {
        return this.caseIgnored;
    }

    /**
     * Set the ignoreCase property: A value indicating whether common words
     * matching will be case insensitive. Default is false.
     *
     * @param caseIgnored the ignoreCase value to set.
     * @return the CommonGramTokenFilter object itself.
     */
    public CommonGramTokenFilter setCaseIgnored(Boolean caseIgnored) {
        this.caseIgnored = caseIgnored;
        return this;
    }

    /**
     * Get the useQueryMode property: A value that indicates whether the token
     * filter is in query mode. When in query mode, the token filter generates
     * bigrams and then removes common words and single terms followed by a
     * common word. Default is false.
     *
     * @return the useQueryMode value.
     */
    public Boolean isQueryModeUsed() {
        return this.queryModeUsed;
    }

    /**
     * Set the useQueryMode property: A value that indicates whether the token
     * filter is in query mode. When in query mode, the token filter generates
     * bigrams and then removes common words and single terms followed by a
     * common word. Default is false.
     *
     * @param queryModeUsed the useQueryMode value to set.
     * @return the CommonGramTokenFilter object itself.
     */
    public CommonGramTokenFilter setQueryModeUsed(Boolean queryModeUsed) {
        this.queryModeUsed = queryModeUsed;
        return this;
    }
}
