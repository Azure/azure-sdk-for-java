// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.util.List;
import java.util.Map;

/**
 * Additional parameters for the API to list metric series definition information for a metric.
 */
@Fluent
public final class ListMetricSeriesDefinitionOptions {
    private Map<String, List<String>> dimensionCombinations;
    private Integer maxPageSize;
    private Integer skip;

    /**
     * Gets the dimension key and values.
     * <p>
     * This enables additional filtering of the metric series for the provided dimension combinations.
     * For example, let's say we've the dimensions 'category' and 'city',
     * so the api can query metric series for the dimension combination 'category', and 'city'.
     * </p>
     *
     * @return The dimension key and values.
     */
    public Map<String, List<String>> getDimensionCombinationsToFilter() {
        return this.dimensionCombinations;
    }

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
     * Sets the dimension key and values.
     * <p>
     * This enables additional filtering of the metric series for the provided dimension combinations.
     * For example, let's say we've the dimensions 'category' and 'city',
     * so the api can query metric series for the dimension combination 'category', and 'city'.
     * </p>
     *
     * @param dimensionCombination The dimension combinations to filter by.
     *
     * @return ListMetricSeriesDefinitionOptions itself.
     */
    public ListMetricSeriesDefinitionOptions setDimensionCombinationToFilter(
        Map<String, List<String>> dimensionCombination) {
        this.dimensionCombinations = dimensionCombination;
        return this;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param maxPageSize The max page size value.
     *
     * @return The ListMetricSeriesDefinitionOptions object itself.
     */
    public ListMetricSeriesDefinitionOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListMetricSeriesDefinitionOptions itself.
     */
    public ListMetricSeriesDefinitionOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }

}
