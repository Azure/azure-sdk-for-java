// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    public void setVariant(String variant) {
        this.variant = variant;
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
    public void setFrom(Double from) {
        this.from = from;
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
    public void setTo(Double to) {
        this.to = to;
    }

}
