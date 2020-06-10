// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Base type for functions that can modify document scores during ranking.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type",
    defaultImpl = ScoringFunction.class)
@JsonTypeName("ScoringFunction")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "distance", value = DistanceScoringFunction.class),
    @JsonSubTypes.Type(name = "freshness", value = FreshnessScoringFunction.class),
    @JsonSubTypes.Type(name = "magnitude", value = MagnitudeScoringFunction.class),
    @JsonSubTypes.Type(name = "tag", value = TagScoringFunction.class)
})
@Fluent
public abstract class ScoringFunction {
    /*
     * The name of the field used as input to the scoring function.
     */
    @JsonProperty(value = "fieldName", required = true)
    private String fieldName;

    /*
     * A multiplier for the raw score. Must be a positive number not equal to
     * 1.0.
     */
    @JsonProperty(value = "boost", required = true)
    private double boost;

    /*
     * A value indicating how boosting will be interpolated across document
     * scores; defaults to "Linear". Possible values include: 'Linear',
     * 'Constant', 'Quadratic', 'Logarithmic'
     */
    @JsonProperty(value = "interpolation")
    private ScoringFunctionInterpolation interpolation;

    /**
     * Get the fieldName property: The name of the field used as input to the
     * scoring function.
     *
     * @return the fieldName value.
     */
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * Set the fieldName property: The name of the field used as input to the
     * scoring function.
     *
     * @param fieldName the fieldName value to set.
     * @return the ScoringFunction object itself.
     */
    public ScoringFunction setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    /**
     * Get the boost property: A multiplier for the raw score. Must be a
     * positive number not equal to 1.0.
     *
     * @return the boost value.
     */
    public double getBoost() {
        return this.boost;
    }

    /**
     * Set the boost property: A multiplier for the raw score. Must be a
     * positive number not equal to 1.0.
     *
     * @param boost the boost value to set.
     * @return the ScoringFunction object itself.
     */
    public ScoringFunction setBoost(double boost) {
        this.boost = boost;
        return this;
    }

    /**
     * Get the interpolation property: A value indicating how boosting will be
     * interpolated across document scores; defaults to "Linear". Possible
     * values include: 'Linear', 'Constant', 'Quadratic', 'Logarithmic'.
     *
     * @return the interpolation value.
     */
    public ScoringFunctionInterpolation getInterpolation() {
        return this.interpolation;
    }

    /**
     * Set the interpolation property: A value indicating how boosting will be
     * interpolated across document scores; defaults to "Linear". Possible
     * values include: 'Linear', 'Constant', 'Quadratic', 'Logarithmic'.
     *
     * @param interpolation the interpolation value to set.
     * @return the ScoringFunction object itself.
     */
    public ScoringFunction setInterpolation(ScoringFunctionInterpolation interpolation) {
        this.interpolation = interpolation;
        return this;
    }
}
