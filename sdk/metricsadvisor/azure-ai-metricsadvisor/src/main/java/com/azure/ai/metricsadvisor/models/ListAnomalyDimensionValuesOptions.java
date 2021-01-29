// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * Describes the additional parameters for the API to list values of a dimension that have anomalies.
 */
public final class ListAnomalyDimensionValuesOptions {
    private Integer top;
    private Integer skip;
    private DimensionKey dimensionToFilter;

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
     * Gets the dimension filter.
     *
     * @return The dimension filter.
     */
    public DimensionKey getDimensionToFilter() {
        return this.dimensionToFilter;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param top The top value.
     * @return The ListDimensionValuesWithAnomaliesOptions object itself.
     */
    public ListAnomalyDimensionValuesOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListDimensionValuesWithAnomaliesOptions object itself.
     */
    public ListAnomalyDimensionValuesOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Sets the dimension filter.
     *
     * This enables additional filtering of dimension values with anomalies being queried.
     * For example, let's say we've the dimensions 'category' and 'city', an incident occurs
     * on 'city=redmond', and we would like to know which categories of product have the problem
     * under 'city=redmond', so the api can query value of the dimension 'category',
     * with dimensionFilter as 'city=redmond'.
     *
     * @param dimensionToFilter The dimension filter.
     *
     * @return ListDimensionValuesWithAnomaliesOptions object itself.
     */
    public ListAnomalyDimensionValuesOptions setDimensionToFilter(DimensionKey dimensionToFilter) {
        this.dimensionToFilter = dimensionToFilter;
        return this;
    }
}
