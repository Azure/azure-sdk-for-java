// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The DataFeedSchema model.
 */
@Fluent
public final class DataFeedSchema {
    private final List<Metric> metrics;
    private List<Dimension> dimensions;
    private String timestampColumn;

    /**
     * @param metrics the metric columns to set.
     */
    public DataFeedSchema(final List<Metric> metrics) {
        this.metrics = metrics;
    }

    /**
     * Get the related metrics list.
     *
     * @return the list of metrics column set.
     */
    public List<Metric> getMetrics() {
        return this.metrics;
    }

    /**
     * Get the dimension list.
     *
     * @return the dimension list
     */
    public List<Dimension> getDimensions() {
        return this.dimensions;
    }

    /**
     * Get the user-defined timestamp column. if timestampColumn is null, start time of every
     * time slice will be used as default value.
     *
     * @return the user-defined timestamp column.
     */
    public String getTimestampColumn() {
        return this.timestampColumn;
    }

    /**
     * Sets the dimension columns value.
     *
     * @param dimensions the dimensions column value to set.
     *
     * @return the DataFeedSchema object itself.
     */
    public DataFeedSchema setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    /**
     * Set the user-defined timestamp column. if timestampColumn is null, start time of every
     * time slice will be used as default value.
     *
     * @param timestampColumnName the user-defined timestamp column.
     *
     * @return the DataFeedSchema object itself.
     */
    public DataFeedSchema setTimestampColumn(String timestampColumnName) {
        this.timestampColumn = timestampColumnName;
        return this;
    }
}
