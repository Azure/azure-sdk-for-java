// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 *
 */
@Fluent
public final class MetricsQueryOptions {
    private String timespan;
    private Duration interval;
    private String aggregation;
    private Integer top;
    private String orderby;
    private String filter;
    private String metricsNamespace;

    /**
     * @return
     */
    public String getTimespan() {
        return timespan;
    }

    /**
     * @param timespan
     *
     * @return
     */
    public MetricsQueryOptions setTimespan(String timespan) {
        this.timespan = timespan;
        return this;
    }

    /**
     * @return
     */
    public Duration getInterval() {
        return interval;
    }

    /**
     * @param interval
     *
     * @return
     */
    public MetricsQueryOptions setInterval(Duration interval) {
        this.interval = interval;
        return this;
    }

    /**
     * @return
     */
    public String getAggregation() {
        return aggregation;
    }

    /**
     * @param aggregation
     *
     * @return
     */
    public MetricsQueryOptions setAggregation(String aggregation) {
        this.aggregation = aggregation;
        return this;
    }

    /**
     * @return
     */
    public Integer getTop() {
        return top;
    }

    /**
     * @param top
     *
     * @return
     */
    public MetricsQueryOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     * @return
     */
    public String getOrderby() {
        return orderby;
    }

    /**
     * @param orderby
     *
     * @return
     */
    public MetricsQueryOptions setOrderby(String orderby) {
        this.orderby = orderby;
        return this;
    }

    /**
     * @return
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @param filter
     *
     * @return
     */
    public MetricsQueryOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * @return
     */
    public String getMetricsNamespace() {
        return metricsNamespace;
    }

    /**
     * @param metricsNamespace
     *
     * @return
     */
    public MetricsQueryOptions setMetricsNamespace(String metricsNamespace) {
        this.metricsNamespace = metricsNamespace;
        return this;
    }
}
