// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.List;

/**
 * The model class to configure the metrics query options.
 */
@Fluent
public final class MetricsQueryOptions {
    private QueryTimeSpan timeSpan;
    private Duration interval;
    private List<AggregationType> aggregation;
    private Integer top;
    private String orderby;
    private String filter;
    private String metricsNamespace;

    /**
     * Returns the timeSpan for which the metrics data is queried.
     * @return the timeSpan for which the metrics data is queried.
     */
    public QueryTimeSpan getTimeSpan() {
        return timeSpan;
    }

    /**
     * Sets the timeSpan for which the metrics data is queried.
     * @param timeSpan the timeSpan for which the metrics data is queried.
     *
     * @return The updated options instance
     */
    public MetricsQueryOptions setTimeSpan(QueryTimeSpan timeSpan) {
        this.timeSpan = timeSpan;
        return this;
    }

    /**
     * Returns the interval (window size) for which the metric data was returned in.
     * @return The interval (window size) for which the metric data was returned in.
     */
    public Duration getInterval() {
        return interval;
    }

    /**
     * Sets the interval (window size) for which the metric data was returned in.
     * @param interval The interval (window size) for which the metric data was returned in.
     *
     * @return The updated options instance
     */
    public MetricsQueryOptions setInterval(Duration interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Returns the list of aggregations that should be applied to the metrics data.
     * @return the list of aggregations that should be applied to the metrics data.
     */
    public List<AggregationType> getAggregation() {
        return aggregation;
    }

    /**
     * Sets the list of aggregations that should be applied to the metrics data.
     * @param aggregation the list of aggregations that should be applied to the metrics data.
     * @return The updated options instance
     */
    public MetricsQueryOptions setAggregation(List<AggregationType> aggregation) {
        this.aggregation = aggregation;
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
    public MetricsQueryOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     * Returns the order in which the query results should be ordered.
     * @return the order in which the query results should be ordered.
     */
    public String getOrderby() {
        return orderby;
    }

    /**
     * Sets the order in which the query results should be ordered.
     * @param orderby the order in which the query results should be ordered.
     *
     * @return The updated options instance
     */
    public MetricsQueryOptions setOrderby(String orderby) {
        this.orderby = orderby;
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
    public MetricsQueryOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Returns the namespace of the metrics been queried
     * @return the namespace of the metrics been queried
     */
    public String getMetricsNamespace() {
        return metricsNamespace;
    }

    /**
     * Sets the namespace of the metrics been queried
     * @param metricsNamespace the namespace of the metrics been queried
     *
     * @return The updated options instance
     */
    public MetricsQueryOptions setMetricsNamespace(String metricsNamespace) {
        this.metricsNamespace = metricsNamespace;
        return this;
    }
}
