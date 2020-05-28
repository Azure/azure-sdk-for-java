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
     * Get the mappings property: A list of mappings of the following format:
     * "a=&gt;b" (all occurrences of the character "a" will be replaced with
     * character "b").
     *
     * @return the mappings value.
     */
    public List<String> getMappings() {
        return this.mappings;
    }

    /**
     * Set the mappings property: A list of mappings of the following format:
     * "a=&gt;b" (all occurrences of the character "a" will be replaced with
     * character "b").
     *
     * @param mappings the mappings value to set.
     * @return the MappingCharFilter object itself.
     */
    public MappingCharFilter setMappings(List<String> mappings) {
        this.mappings = mappings;
        return this;
    }
}
