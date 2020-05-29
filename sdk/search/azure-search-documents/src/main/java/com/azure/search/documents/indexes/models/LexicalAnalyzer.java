// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Base type for analyzers.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type",
    defaultImpl = LexicalAnalyzer.class)
@JsonTypeName("LexicalAnalyzer")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.CustomAnalyzer", value = CustomAnalyzer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.PatternAnalyzer", value = PatternAnalyzer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.StandardAnalyzer", value = LuceneStandardAnalyzer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.StopAnalyzer", value = StopAnalyzer.class)
})
@Fluent
public abstract class LexicalAnalyzer {
    /*
     * The name of the analyzer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /**
     * Get the name property: The name of the analyzer. It must only contain
     * letters, digits, spaces, dashes or underscores, can only start and end
     * with alphanumeric characters, and is limited to 128 characters.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The name of the analyzer. It must only contain
     * letters, digits, spaces, dashes or underscores, can only start and end
     * with alphanumeric characters, and is limited to 128 characters.
     *
     * @param name the name value to set.
     * @return the LexicalAnalyzer object itself.
     */
    public LexicalAnalyzer setName(String name) {
        this.name = name;
        return this;
    }
}
