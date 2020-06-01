// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * Flexibly separates text into terms via a regular expression pattern. This
 * analyzer is implemented using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.PatternAnalyzer")
@Fluent
public final class PatternAnalyzer extends LexicalAnalyzer {
    /*
     * A value indicating whether terms should be lower-cased. Default is true.
     */
    @JsonProperty(value = "lowercase")
    private Boolean lowerCaseTerms;

    /*
     * A regular expression pattern to match token separators. Default is an
     * expression that matches one or more non-word characters.
     */
    @JsonProperty(value = "pattern")
    private String pattern;

    /*
     * Regular expression flags.
     */
    @JsonProperty(value = "flags")
    private List<RegexFlags> flags;

    /*
     * A list of stopwords.
     */
    @JsonProperty(value = "stopwords")
    private List<String> stopwords;

    /**
     * Get the lowerCaseTerms property: A value indicating whether terms should
     * be lower-cased. Default is true.
     *
     * @return the lowerCaseTerms value.
     */
    public Boolean isLowerCaseTerms() {
        return this.lowerCaseTerms;
    }

    /**
     * Set the lowerCaseTerms property: A value indicating whether terms should
     * be lower-cased. Default is true.
     *
     * @param lowerCaseTerms the lowerCaseTerms value to set.
     * @return the PatternAnalyzer object itself.
     */
    public PatternAnalyzer setLowerCaseTerms(Boolean lowerCaseTerms) {
        this.lowerCaseTerms = lowerCaseTerms;
        return this;
    }

    /**
     * Get the pattern property: A regular expression pattern to match token
     * separators. Default is an expression that matches one or more non-word
     * characters.
     *
     * @return the pattern value.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Set the pattern property: A regular expression pattern to match token
     * separators. Default is an expression that matches one or more non-word
     * characters.
     *
     * @param pattern the pattern value to set.
     * @return the PatternAnalyzer object itself.
     */
    public PatternAnalyzer setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Get the flags property: Regular expression flags.
     *
     * @return the flags value.
     */
    public List<RegexFlags> getFlags() {
        return this.flags;
    }

    /**
     * Set the flags property: Regular expression flags.
     *
     * @param flags the flags value to set.
     * @return the PatternAnalyzer object itself.
     */
    public PatternAnalyzer setFlags(List<RegexFlags> flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Get the stopwords property: A list of stopwords.
     *
     * @return the stopwords value.
     */
    public List<String> getStopwords() {
        return this.stopwords;
    }

    /**
     * Set the stopwords property: A list of stopwords.
     *
     * @param stopwords the stopwords value to set.
     * @return the PatternAnalyzer object itself.
     */
    public PatternAnalyzer setStopwords(List<String> stopwords) {
        this.stopwords = stopwords;
        return this;
    }
}
