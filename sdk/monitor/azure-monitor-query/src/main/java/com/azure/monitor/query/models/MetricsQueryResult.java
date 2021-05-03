// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.time.Duration;
import java.util.List;

/**
 *
 */
@Immutable
public final class MetricsQueryResult {

    private final Integer cost;
    private final String timeSpan;
    private final Duration interval;
    private final String namespace;
    private final String resourceRegion;
    private final List<Metrics> metrics;

    /**
     * @param cost
     * @param timeSpan
     * @param interval
     * @param namespace
     * @param resourceRegion
     * @param metrics
     */
    public MetricsQueryResult(Integer cost, String timeSpan, Duration interval, String namespace, String resourceRegion, List<Metrics> metrics) {
        this.cost = cost;
        this.timeSpan = timeSpan;
        this.interval = interval;
        this.namespace = namespace;
        this.resourceRegion = resourceRegion;
        this.metrics = metrics;
    }

    /**
     * @return
     */
    public Integer getCost() {
        return cost;
    }

    /**
     * @return
     */
    public String getTimeSpan() {
        return timeSpan;
    }

    /**
     * @return
     */
    public Duration getInterval() {
        return interval;
    }

    /**
     * @return
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return
     */
    public String getResourceRegion() {
        return resourceRegion;
    }

    /**
     * @return
     */
    public List<Metrics> getMetrics() {
        return metrics;
    }
}
