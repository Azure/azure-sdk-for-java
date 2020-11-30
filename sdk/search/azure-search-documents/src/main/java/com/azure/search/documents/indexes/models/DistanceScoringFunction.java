// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a function that boosts scores based on distance from a geographic
 * location.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("distance")
@Fluent
public final class DistanceScoringFunction extends ScoringFunction {
    /*
     * Parameter values for the distance scoring function.
     */
    @JsonProperty(value = "distance", required = true)
    private DistanceScoringParameters parameters;

    /**
     * Constructor of {@link ScoringFunction}.
     *
     * @param fieldName The name of the field used as input to the scoring function.
     * @param boost A multiplier for the raw score. Must be a positive number not equal to 1.0.
     * @param parameters Parameter values for the distance scoring function.
     */
    @JsonCreator
    public DistanceScoringFunction(
        @JsonProperty(value = "fieldName") String fieldName,
        @JsonProperty(value = "boost") double boost,
        @JsonProperty(value = "distance") DistanceScoringParameters parameters) {
        super(fieldName, boost);
        this.parameters = parameters;
    }

    /**
     * Get the parameters property: Parameter values for the distance scoring
     * function.
     *
     * @return the parameters value.
     */
    public DistanceScoringParameters getParameters() {
        return this.parameters;
    }
}
