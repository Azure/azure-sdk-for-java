// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Percentile allocation of a variant. Contains a variant and a range of users assigned to the variant.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PercentileAllocation {

    private String variant;

    private Double from;

    private Double to;

    /**
     * @return the variant
     */
    public String getVariant() {
        return variant;
    }

    /**
     * @param variant the variant to set
     */
    public PercentileAllocation setVariant(String variant) {
        this.variant = variant;
        return this;
    }

    /**
     * @return the from
     */
    public Double getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public PercentileAllocation setFrom(Double from) {
        this.from = from;
        return this;
    }

    /**
     * @return the to
     */
    public Double getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public PercentileAllocation setTo(Double to) {
        this.to = to;
        return this;
    }

}
