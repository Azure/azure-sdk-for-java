// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * A character filter that applies mappings defined with the mappings option.
 * Matching is greedy (longest pattern matching at a given point wins).
 * Replacement is allowed to be the empty string. This character filter is
 * implemented using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.MappingCharFilter")
@Fluent
public final class MappingCharFilter extends CharFilter {
    /*
     * A list of mappings of the following format: "a=>b" (all occurrences of
     * the character "a" will be replaced with character "b").
     */
    @JsonProperty(value = "mappings", required = true)
    private List<String> mappings;

    /**
     * Constructor of {@link MappingCharFilter}.
     *
     * @param name The name of the char filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     * @param mappings A list of mappings of the following format: "a=%3Eb" (all occurrences of
     * the character "a" will be replaced with character "b").
     */
    public MappingCharFilter(String name, List<String> mappings) {
        super(name);
        this.mappings = mappings;
    }

    /**
     * Get the mappings property: A list of mappings of the following format:
     * "a=&gt;b" (all occurrences of the character "a" will be replaced with
     * character "b").
     *
     * @return the mappings value.
     */
    public List<String> getMappings() {
        return this.mappings;
    }
}
