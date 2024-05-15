// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import java.time.OffsetDateTime;

/**
 * Optional parameters for listing the usage metrics, aggregated by Batch Pool across
 * individual time intervals, for the specified Batch Account.
 */
public class ListBatchPoolUsageMetricsOptions extends BatchBaseOptions {
    private OffsetDateTime endtime;
    private String filter;
    private OffsetDateTime starttime;

    /**
     * Gets the latest time from which to include metrics.
     *
     * <p>This property represents the end time for including metrics in an operation.
     * It must be at least two hours before the current time.
     * If not specified, it defaults to the end time of the last aggregation interval currently available.
     *
     * @return The latest time from which to include metrics.
     */
    public OffsetDateTime getEndTime() {
        return endtime;
    }

    /**
     * Sets the latest time from which to include metrics.
     *
     * <p>This property represents the end time for including metrics in an operation.
     * It must be at least two hours before the current time.
     * If not specified, it defaults to the end time of the last aggregation interval currently available.
     *
     * @param endtime The latest time from which to include metrics.
     * @return The {@link ListBatchPoolUsageMetricsOptions} object itself, allowing for method chaining.
     */
    public ListBatchPoolUsageMetricsOptions setEndTime(OffsetDateTime endtime) {
        this.endtime = endtime;
        return this;
    }

    /**
     * Gets the OData $filter clause used for filtering results.
     *
     * @return The OData $filter clause.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the OData $filter clause used for filtering results.
     *
     * @param filter The OData $filter clause.
     * @return The {@link ListBatchPoolUsageMetricsOptions} object itself, allowing for method chaining.
     */
    public ListBatchPoolUsageMetricsOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Gets the earliest time from which to include metrics.
     *
     * This property represents the start time for including metrics in an operation.
     * It must be at least two and a half hours before the current time.
     * If not specified, it defaults to the start time of the last aggregation interval currently available.
     *
     * @return The earliest time from which to include metrics.
     */
    public OffsetDateTime getStartTime() {
        return starttime;
    }

    /**
     * Sets the earliest time from which to include metrics.
     *
     * This property represents the start time for including metrics in an operation.
     * It must be at least two and a half hours before the current time.
     * If not specified, it defaults to the start time of the last aggregation interval currently available.
     *
     * @param starttime The earliest time from which to include metrics.
     * @return The {@link ListBatchPoolUsageMetricsOptions} object itself, allowing for method chaining.
     */
    public ListBatchPoolUsageMetricsOptions setStartTime(OffsetDateTime starttime) {
        this.starttime = starttime;
        return this;
    }

}
