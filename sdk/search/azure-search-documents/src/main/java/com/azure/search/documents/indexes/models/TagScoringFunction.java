// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a function that boosts scores of documents with string values
 * matching a given list of tags.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("tag")
@Fluent
public final class TagScoringFunction extends ScoringFunction {
    /*
     * Parameter values for the tag scoring function.
     */
    @JsonProperty(value = "tag", required = true)
    private TagScoringParameters parameters;

    /**
     * Constructor of {@link TagScoringFunction}.
     *
     * @param fieldName The name of the field used as input to the scoring function.
     * @param boost A multiplier for the raw score. Must be a positive number not equal to 1.0.
     * @param parameters Parameter values for the tag scoring function.
     */
    @JsonCreator
    public TagScoringFunction(
        @JsonProperty(value = "fieldName") String fieldName,
        @JsonProperty(value = "boost") double boost,
        @JsonProperty(value = "tag") TagScoringParameters parameters) {
        super(fieldName, boost);
        this.parameters = parameters;
    }

    /**
     * Get the parameters property: Parameter values for the tag scoring
     * function.
     *
     * @return the parameters value.
     */
    public TagScoringParameters getParameters() {
        return this.parameters;
    }

}
