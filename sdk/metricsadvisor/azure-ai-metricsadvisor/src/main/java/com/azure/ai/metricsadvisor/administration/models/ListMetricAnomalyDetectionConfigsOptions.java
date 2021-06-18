// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Additional parameters to set when listing anomaly detection configurations.
 */
@Fluent
public final class ListMetricAnomalyDetectionConfigsOptions {
    private Integer maxPageSize;
    private Integer skip;

    /**
     * Gets limit indicating the number of items that will be included in a service returned page.
     *
     * @return The max page size value.
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
    }

    /**
     * Gets the number of items in the queried collection that will be skipped and not included
     * in the returned result.
     *
     * @return The skip value.
     */
    public Integer getSkip() {
        return this.skip;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param maxPageSize The max page size value.
     *
     * @return The ListMetricAnomalyDetectionConfigsOptions object itself.
     */
    public ListMetricAnomalyDetectionConfigsOptions setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListMetricAnomalyDetectionConfigsOptions itself.
     */
    public ListMetricAnomalyDetectionConfigsOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }
}
