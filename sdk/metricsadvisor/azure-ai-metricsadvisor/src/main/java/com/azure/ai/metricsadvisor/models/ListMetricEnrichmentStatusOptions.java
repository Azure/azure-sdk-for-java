// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * Additional parameters to set when listing enrichment status for a metric.
 */
@Fluent
public final class ListMetricEnrichmentStatusOptions {
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private Integer top;
    private Integer skip;

    /**
     * Creates a new instance of ListMetricEnrichmentStatusOptions.
     *
     * @param startTime The start time for querying the time series data.
     * @param endTime The end time for querying the time series data.
     */
    public ListMetricEnrichmentStatusOptions(OffsetDateTime startTime,
        OffsetDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
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
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param top The top value.
     *
     * @return The ListDataFeedOptions object itself.
     */
    public ListMetricEnrichmentStatusOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListMetricEnrichmentStatusOptions itself.
     */
    public ListMetricEnrichmentStatusOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Gets the start time for which the time series data will be queried for.
     *
     * @return The start time.
     */
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the end time for which the time series data will be queried for.
     *
     * @return The end time.
     */
    public OffsetDateTime getEndTime() {
        return this.endTime;
    }
}
