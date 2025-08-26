// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Percentile allocation of a variant for feature flag targeting. This class defines how users
 * are assigned to a specific variant based on a percentage range. It allows for gradual rollout
 * of features to a specific percentage of users by defining a variant name and a numeric range
 * (from-to) that determines which portion of users receive this variant when the feature is
 * evaluated.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PercentileAllocation {

    /**
     * Creates a new instance of the PercentileAllocation class.
     */
    public PercentileAllocation() {
    }

    /**
     * The name of the variant that will be assigned to users within the specified
     * percentage range. This corresponds to a variant defined in the feature flag
     * configuration.
     */
    private String variant;

    /**
     * The lower bound (inclusive) of the percentage range for this variant allocation.
     * Users with a computed hash value >= this percentage will be assigned to this
     * variant. The value should be between 0.0 and 100.0.
     */
    private Double from;

    /**
     * The upper bound of the percentage range for this variant allocation.
     * <p>
     * This value is exclusive (users with computed hash values strictly less than this value will be assigned to this variant),
     * except when set to 100, where it becomes inclusive. The value should be between 0.0 and 100.0 and greater than the
     * 'from' value.
     * </p>
     */
    private Double to;

    /**
     * Gets the name of the variant that is assigned to users within the specified
     * percentage range.
     * 
     * @return the variant name for this percentile allocation
     */
    public String getVariant() {
        return variant;
    }

    /**
     * Sets the name of the variant that should be assigned to users within the
     * specified percentage range. This should match a valid variant name defined
     * in the feature flag configuration.
     * 
     * @param variant the variant name to assign for this percentile range
     * @return the updated PercentileAllocation instance for method chaining
     */
    public PercentileAllocation setVariant(String variant) {
        this.variant = variant;
        return this;
    }

    /**
     * Gets the lower bound of the percentage range for this variant allocation.
     * This represents the starting point of the percentile range where users will be
     * assigned to this variant. The value is inclusive and typically between 0.0 and 100.0.
     * 
     * @return the lower bound percentage value for this allocation
     */
    public Double getFrom() {
        return from;
    }

    /**
     * Sets the lower bound of the percentage range for this variant allocation.
     * This value is inclusive (users with computed hash values greater than or equal to this 
     * value will be assigned to this variant). The value should be between 0.0 and 100.0 
     * and less than the 'to' value.
     * 
     * @param from the lower bound percentage value to set for this allocation
     * @return the updated PercentileAllocation instance for method chaining
     */
    public PercentileAllocation setFrom(Double from) {
        this.from = from;
        return this;
    }


    /**
     * Gets the upper bound of the percentage range for this variant allocation.
     * <p>
     * This value is exclusive (users with computed hash values strictly less than this value will be assigned to this variant),
     * except when set to 100, where it becomes inclusive. The value should be between 0.0 and 100.0 and greater than the
     * 'from' value.
     * </p>
     * 
     * @return the upper bound percentage value for this allocation
     */
    public Double getTo() {
        return to;
    }

    /**
     * Sets the upper bound of the percentage range for this variant allocation.
     * <p>
     * This value is exclusive (users with computed hash values strictly less than this value will be assigned to this variant),
     * except when set to 100, where it becomes inclusive. The value should be between 0.0 and 100.0 and greater than the
     * 'from' value.
     * </p>
     * 
     * @param to the upper bound percentage value to set for this allocation
     * @return the updated PercentileAllocation instance for method chaining
     */
    public PercentileAllocation setTo(Double to) {
        this.to = to;
        return this;
    }

}
