// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Marks terms as keywords. This token filter is implemented using Apache
 * Lucene.
 */
@Fluent
public final class KeywordMarkerTokenFilter extends TokenFilter {
    /*
     * A list of words to mark as keywords.
     */
    private final List<String> keywords;

    /*
     * A value indicating whether to ignore case. If true, all words are
     * converted to lower case first. Default is false.
     */
    private Boolean caseIgnored;

    /**
     * Constructor of {@link KeywordMarkerTokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     * @param keywords A list of words to mark as keywords.
     */
    public KeywordMarkerTokenFilter(String name, List<String> keywords) {
        super(name);
        this.keywords = keywords;
    }

    /**
     * Get the keywords property: A list of words to mark as keywords.
     *
     * @return the keywords value.
     */
    public List<String> getKeywords() {
        return this.keywords;
    }

    /**
     * Get the ignoreCase property: A value indicating whether to ignore case.
     * If true, all words are converted to lower case first. Default is false.
     *
     * @return the ignoreCase value.
     */
    public Boolean isCaseIgnored() {
        return this.caseIgnored;
    }

    /**
     * Set the ignoreCase property: A value indicating whether to ignore case.
     * If true, all words are converted to lower case first. Default is false.
     *
     * @param caseIgnored the ignoreCase value to set.
     * @return the KeywordMarkerTokenFilter object itself.
     */
    public KeywordMarkerTokenFilter setCaseIgnored(Boolean caseIgnored) {
        this.caseIgnored = caseIgnored;
        return this;
    }
}
