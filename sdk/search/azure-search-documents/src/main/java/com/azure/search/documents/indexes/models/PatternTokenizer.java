// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Arrays;
import java.util.List;

/**
 * Tokenizer that uses regex pattern matching to construct distinct tokens.
 * This tokenizer is implemented using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.PatternTokenizer")
@Fluent
public final class PatternTokenizer extends LexicalTokenizer {
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
     * The zero-based ordinal of the matching group in the regular expression
     * pattern to extract into tokens. Use -1 if you want to use the entire
     * pattern to split the input into tokens, irrespective of matching groups.
     * Default is -1.
     */
    @JsonProperty(value = "group")
    private Integer group;

    /**
     * Constructor of {@link PatternTokenizer}.
     *
     * @param name The name of the tokenizer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public PatternTokenizer(String name) {
        super(name);
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
     * @return the PatternTokenizer object itself.
     */
    public PatternTokenizer setPattern(String pattern) {
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
     * @return the PatternTokenizer object itself.
     */
    public PatternTokenizer setFlags(RegexFlags... flags) {
        this.flags = (flags == null) ? null : Arrays.asList(flags);
        return this;
    }

    /**
     * Set the flags property: Regular expression flags.
     *
     * @param flags the flags value to set.
     * @return the PatternTokenizer object itself.
     */
    @JsonSetter
    public PatternTokenizer setFlags(List<RegexFlags> flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Get the group property: The zero-based ordinal of the matching group in
     * the regular expression pattern to extract into tokens. Use -1 if you
     * want to use the entire pattern to split the input into tokens,
     * irrespective of matching groups. Default is -1.
     *
     * @return the group value.
     */
    public Integer getGroup() {
        return this.group;
    }

    /**
     * Set the group property: The zero-based ordinal of the matching group in
     * the regular expression pattern to extract into tokens. Use -1 if you
     * want to use the entire pattern to split the input into tokens,
     * irrespective of matching groups. Default is -1.
     *
     * @param group the group value to set.
     * @return the PatternTokenizer object itself.
     */
    public PatternTokenizer setGroup(Integer group) {
        this.group = group;
        return this;
    }
}
