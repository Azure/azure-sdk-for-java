// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * Marks terms as keywords. This token filter is implemented using Apache
 * Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.KeywordMarkerTokenFilter")
@Fluent
public final class KeywordMarkerTokenFilter extends TokenFilter {
    /*
     * A list of words to mark as keywords.
     */
    @JsonProperty(value = "keywords", required = true)
    private List<String> keywords;

    /*
     * A value indicating whether to ignore case. If true, all words are
     * converted to lower case first. Default is false.
     */
    @JsonProperty(value = "ignoreCase")
    private Boolean caseIgnored;

    /**
     * Get the keywords property: A list of words to mark as keywords.
     *
     * @return the keywords value.
     */
    public List<String> getKeywords() {
        return this.keywords;
    }

    /**
     * Set the keywords property: A list of words to mark as keywords.
     *
     * @param keywords the keywords value to set.
     * @return the KeywordMarkerTokenFilter object itself.
     */
    public KeywordMarkerTokenFilter setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
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
