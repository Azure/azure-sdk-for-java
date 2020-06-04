// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides parameter values to a distance scoring function.
 */
@Fluent
public final class DistanceScoringParameters {
    /*
     * The name of the parameter passed in search queries to specify the
     * reference location.
     */
    @JsonProperty(value = "referencePointParameter", required = true)
    private String referencePointParameter;

    /*
     * The distance in kilometers from the reference location where the
     * boosting range ends.
     */
    @JsonProperty(value = "boostingDistance", required = true)
    private double boostingDistance;

    /**
     * Get the referencePointParameter property: The name of the parameter
     * passed in search queries to specify the reference location.
     *
     * @return the referencePointParameter value.
     */
    public String getReferencePointParameter() {
        return this.referencePointParameter;
    }

    /**
     * Set the referencePointParameter property: The name of the parameter
     * passed in search queries to specify the reference location.
     *
     * @param referencePointParameter the referencePointParameter value to set.
     * @return the DistanceScoringParameters object itself.
     */
    public DistanceScoringParameters setReferencePointParameter(String referencePointParameter) {
        this.referencePointParameter = referencePointParameter;
        return this;
    }

    /**
     * Get the boostingDistance property: The distance in kilometers from the
     * reference location where the boosting range ends.
     *
     * @return the boostingDistance value.
     */
    public double getBoostingDistance() {
        return this.boostingDistance;
    }

    /**
     * Set the boostingDistance property: The distance in kilometers from the
     * reference location where the boosting range ends.
     *
     * @param boostingDistance the boostingDistance value to set.
     * @return the DistanceScoringParameters object itself.
     */
    public DistanceScoringParameters setBoostingDistance(double boostingDistance) {
        this.boostingDistance = boostingDistance;
        return this;
    }
}
