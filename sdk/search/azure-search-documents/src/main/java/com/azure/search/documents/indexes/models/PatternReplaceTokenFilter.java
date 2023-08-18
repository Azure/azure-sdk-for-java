// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A character filter that replaces characters in the input string. It uses a
 * regular expression to identify character sequences to preserve and a
 * replacement pattern to identify characters to replace. For example, given
 * the input text "aa bb aa bb", pattern "(aa)\s+(bb)", and replacement
 * "$1#$2", the result would be "aa#bb aa#bb". This token filter is implemented
 * using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.PatternReplaceTokenFilter")
@Fluent
public final class PatternReplaceTokenFilter extends TokenFilter {
    /*
     * A regular expression pattern.
     */
    @JsonProperty(value = "pattern", required = true)
    private String pattern;

    /*
     * The replacement text.
     */
    @JsonProperty(value = "replacement", required = true)
    private String replacement;

    /**
     * Constructor of {@link PatternReplaceTokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     * @param pattern A regular expression pattern.
     * @param replacement The replacement text.
     */
    public PatternReplaceTokenFilter(String name, String pattern, String replacement) {
        super(name);
        this.pattern = pattern;
        this.replacement = replacement;
    }

    /**
     * Get the pattern property: A regular expression pattern.
     *
     * @return the pattern value.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Get the replacement property: The replacement text.
     *
     * @return the replacement value.
     */
    public String getReplacement() {
        return this.replacement;
    }

}
