// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

/**
 * Additional properties to set when using the API to list dimension values for a metric.
 */
@Fluent
public final class ListMetricDimensionValuesOptions {
    private Integer top;
    private Integer skip;
    private String dimensionValueToFilter;

    /**
     * Get the dimension value to filter the query result.
     *
     * @return the dimension value to filter the query result.
     */
    public String getDimensionValueToFilter() {
        return this.dimensionValueToFilter;
    }

    /**
     * Gets limit indicating the number of items that will be included in a service returned page.
     *
     * @return The top value.
     */
    public Integer getTop() {
        return this.top;
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
     * Set the dimension value to filter the query result.
     *
     * @param dimensionValueToFilter the dimension value to filter the query result.
     *
     * @return the ListMetricDimensionValuesOptions itself.
     */
    public ListMetricDimensionValuesOptions setDimensionValueToFilter(String dimensionValueToFilter) {
        this.dimensionValueToFilter = dimensionValueToFilter;
        return this;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param top The top value.
     *
     * @return The ListMetricDimensionValuesOptions object itself.
     */
    public ListMetricDimensionValuesOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     *
     * @return ListMetricDimensionValuesOptions itself.
     */
    public ListMetricDimensionValuesOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }
}
