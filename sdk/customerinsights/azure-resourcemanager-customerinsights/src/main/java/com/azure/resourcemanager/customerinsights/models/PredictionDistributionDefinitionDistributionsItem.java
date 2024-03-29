// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.customerinsights.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The definition of a prediction distribution. */
@Fluent
public final class PredictionDistributionDefinitionDistributionsItem {
    /*
     * Score threshold.
     */
    @JsonProperty(value = "scoreThreshold")
    private Integer scoreThreshold;

    /*
     * Number of positives.
     */
    @JsonProperty(value = "positives")
    private Long positives;

    /*
     * Number of negatives.
     */
    @JsonProperty(value = "negatives")
    private Long negatives;

    /*
     * Number of positives above threshold.
     */
    @JsonProperty(value = "positivesAboveThreshold")
    private Long positivesAboveThreshold;

    /*
     * Number of negatives above threshold.
     */
    @JsonProperty(value = "negativesAboveThreshold")
    private Long negativesAboveThreshold;

    /** Creates an instance of PredictionDistributionDefinitionDistributionsItem class. */
    public PredictionDistributionDefinitionDistributionsItem() {
    }

    /**
     * Get the scoreThreshold property: Score threshold.
     *
     * @return the scoreThreshold value.
     */
    public Integer scoreThreshold() {
        return this.scoreThreshold;
    }

    /**
     * Set the scoreThreshold property: Score threshold.
     *
     * @param scoreThreshold the scoreThreshold value to set.
     * @return the PredictionDistributionDefinitionDistributionsItem object itself.
     */
    public PredictionDistributionDefinitionDistributionsItem withScoreThreshold(Integer scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
        return this;
    }

    /**
     * Get the positives property: Number of positives.
     *
     * @return the positives value.
     */
    public Long positives() {
        return this.positives;
    }

    /**
     * Set the positives property: Number of positives.
     *
     * @param positives the positives value to set.
     * @return the PredictionDistributionDefinitionDistributionsItem object itself.
     */
    public PredictionDistributionDefinitionDistributionsItem withPositives(Long positives) {
        this.positives = positives;
        return this;
    }

    /**
     * Get the negatives property: Number of negatives.
     *
     * @return the negatives value.
     */
    public Long negatives() {
        return this.negatives;
    }

    /**
     * Set the negatives property: Number of negatives.
     *
     * @param negatives the negatives value to set.
     * @return the PredictionDistributionDefinitionDistributionsItem object itself.
     */
    public PredictionDistributionDefinitionDistributionsItem withNegatives(Long negatives) {
        this.negatives = negatives;
        return this;
    }

    /**
     * Get the positivesAboveThreshold property: Number of positives above threshold.
     *
     * @return the positivesAboveThreshold value.
     */
    public Long positivesAboveThreshold() {
        return this.positivesAboveThreshold;
    }

    /**
     * Set the positivesAboveThreshold property: Number of positives above threshold.
     *
     * @param positivesAboveThreshold the positivesAboveThreshold value to set.
     * @return the PredictionDistributionDefinitionDistributionsItem object itself.
     */
    public PredictionDistributionDefinitionDistributionsItem withPositivesAboveThreshold(Long positivesAboveThreshold) {
        this.positivesAboveThreshold = positivesAboveThreshold;
        return this;
    }

    /**
     * Get the negativesAboveThreshold property: Number of negatives above threshold.
     *
     * @return the negativesAboveThreshold value.
     */
    public Long negativesAboveThreshold() {
        return this.negativesAboveThreshold;
    }

    /**
     * Set the negativesAboveThreshold property: Number of negatives above threshold.
     *
     * @param negativesAboveThreshold the negativesAboveThreshold value to set.
     * @return the PredictionDistributionDefinitionDistributionsItem object itself.
     */
    public PredictionDistributionDefinitionDistributionsItem withNegativesAboveThreshold(Long negativesAboveThreshold) {
        this.negativesAboveThreshold = negativesAboveThreshold;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }
}
