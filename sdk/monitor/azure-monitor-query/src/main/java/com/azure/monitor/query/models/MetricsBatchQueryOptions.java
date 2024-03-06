// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.List;

/**
 * The model class to configure the metrics batch query options.
 */
@Fluent
public final class MetricsBatchQueryOptions {
    private QueryTimeInterval timeInterval;
    private Duration granularity;
    private List<AggregationType> aggregations;
    private Integer top;
    private String orderBy;
    private String filter;


    /**
     * Returns the time span for which the metrics data is queried.
     * @return the time span for which the metrics data is queried.
     */
    public QueryTimeInterval getTimeInterval() {
        return timeInterval;
    }

    /**
     * Sets the time span for which the metrics data is queried.
     * @param timeInterval the time span for which the metrics data is queried.
     *
     * @return The updated options instance
     */
    public MetricsBatchQueryOptions setTimeInterval(QueryTimeInterval timeInterval) {
        this.timeInterval = timeInterval;
        return this;
    }

    /**
     * Returns the interval (window size) for which the metric data was returned in.
     * @return The interval (window size) for which the metric data was returned in.
     */
    public Duration getGranularity() {
        return granularity;
    }

    /**
     * Sets the interval (window size) for which the metric data was returned in.
     * @param granularity The interval (window size) for which the metric data was returned in.
     *
     * @return The updated options instance
     */
    public MetricsBatchQueryOptions setGranularity(Duration granularity) {
        this.granularity = granularity;
        return this;
    }

    /**
     * Returns the list of aggregations that should be applied to the metrics data.
     * @return the list of aggregations that should be applied to the metrics data.
     */
    public List<AggregationType> getAggregations() {
        return aggregations;
    }

    /**
     * Sets the list of aggregations that should be applied to the metrics data.
     * @param aggregations the list of aggregations that should be applied to the metrics data.
     * @return The updated options instance
     */
    public MetricsBatchQueryOptions setAggregations(List<AggregationType> aggregations) {
        this.aggregations = aggregations;
        return this;
    }

    /**
     * Returns the number of top metrics values to query.
     * @return the number of top metrics values to query.
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Sets the number of top metrics values to query.
     * @param top the number of top metrics values to query.
     *
     * @return The updated options instance
     */
    public MetricsBatchQueryOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     * Returns the order in which the query results should be ordered.
     * @return the order in which the query results should be ordered.
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the order in which the query results should be ordered.
     * @param orderBy the order in which the query results should be ordered.
     *
     * @return The updated options instance
     */
    public MetricsBatchQueryOptions setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * Returns the filter to be applied to the query. The filter users OData format.
     * @return the filter to be applied to the query. The filter users OData format.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the filter to be applied to the query. The filter users OData format.
     * @param filter the filter to be applied to the query. The filter users OData format.
     *
     * @return The updated options instance
     */
    public MetricsBatchQueryOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }
}
