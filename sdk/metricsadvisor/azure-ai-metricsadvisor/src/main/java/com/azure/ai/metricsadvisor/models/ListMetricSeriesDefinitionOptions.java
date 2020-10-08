// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Additional parameters for the API to list metric series definition information for a metric.
 */
@Fluent
public final class ListMetricSeriesDefinitionOptions {
    private final OffsetDateTime activeSince;
    private Map<String, List<String>> dimensionCombinations;
    private Integer top;
    private Integer skip;

    /**
     * Create an instance of ListMetricSeriesDefinitionOptions object.
     *
     * @param activeSince the start time for querying series ingested after this time.
     */
    public ListMetricSeriesDefinitionOptions(OffsetDateTime activeSince) {
        this.activeSince = activeSince;
    }

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
     * @param top The top value.
     *
     * @return The ListMetricSeriesDefinitionOptions object itself.
     */
    public ListMetricSeriesDefinitionOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListMetricSeriesDefinitionOptions itself.
     */
    public ListMetricSeriesDefinitionOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Get the start time for querying series ingested after this time.
     *
     * @return the activeSince value.
     */
    public OffsetDateTime getActiveSince() {
        return this.activeSince;
    }
}
