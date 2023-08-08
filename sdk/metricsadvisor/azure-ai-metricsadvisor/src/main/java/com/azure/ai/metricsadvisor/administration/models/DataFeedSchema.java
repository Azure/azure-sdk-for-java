// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The DataFeedSchema model.
 */
@Fluent
public final class DataFeedSchema {
    private final List<DataFeedMetric> dataFeedMetrics;
    private List<DataFeedDimension> dataFeedDimensions;
    private String timestampColumn;

    /**
     * @param dataFeedMetrics the metric columns to set.
     */
    public DataFeedSchema(final List<DataFeedMetric> dataFeedMetrics) {
        this.dataFeedMetrics = dataFeedMetrics;
    }

    /**
     * Get the related dataFeedMetrics list.
     *
     * @return the list of dataFeedMetrics column set.
     */
    public List<DataFeedMetric> getMetrics() {
        return this.dataFeedMetrics;
    }

    /**
     * Get the dimension list.
     *
     * @return the dimension list
     */
    public List<DataFeedDimension> getDimensions() {
        return this.dataFeedDimensions;
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
    public DataFeedSchema setDimensions(List<DataFeedDimension> dimensions) {
        this.dataFeedDimensions = dimensions;
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
