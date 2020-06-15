// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * Uses Java regexes to emit multiple tokens - one for each capture group in
 * one or more patterns. This token filter is implemented using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.PatternCaptureTokenFilter")
@Fluent
public final class PatternCaptureTokenFilter extends TokenFilter {
    /*
     * A list of patterns to match against each token.
     */
    @JsonProperty(value = "patterns", required = true)
    private List<String> patterns;

    /*
     * A value indicating whether to return the original token even if one of
     * the patterns matches. Default is true.
     */
    @JsonProperty(value = "preserveOriginal")
    private Boolean preserveOriginal;

    /**
     * Get the patterns property: A list of patterns to match against each
     * token.
     *
     * @return the patterns value.
     */
    public List<String> getPatterns() {
        return this.patterns;
    }

    /**
     * Set the patterns property: A list of patterns to match against each
     * token.
     *
     * @param patterns the patterns value to set.
     * @return the PatternCaptureTokenFilter object itself.
     */
    public PatternCaptureTokenFilter setPatterns(List<String> patterns) {
        this.patterns = patterns;
        return this;
    }

    /**
     * Get the preserveOriginal property: A value indicating whether to return
     * the original token even if one of the patterns matches. Default is true.
     *
     * @return the preserveOriginal value.
     */
    public Boolean isPreserveOriginal() {
        return this.preserveOriginal;
    }

    /**
     * Set the preserveOriginal property: A value indicating whether to return
     * the original token even if one of the patterns matches. Default is true.
     *
     * @param preserveOriginal the preserveOriginal value to set.
     * @return the PatternCaptureTokenFilter object itself.
     */
    public PatternCaptureTokenFilter setPreserveOriginal(Boolean preserveOriginal) {
        this.preserveOriginal = preserveOriginal;
        return this;
    }
}
