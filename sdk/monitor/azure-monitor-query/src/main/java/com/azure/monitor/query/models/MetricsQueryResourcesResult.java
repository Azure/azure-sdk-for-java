// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import java.util.List;

/**
 * The result of a metrics query batch. It contains the results of individual queries.
 */
public final class MetricsQueryResourcesResult {
    private final List<MetricsQueryResult> metricsQueryResults;

    /**
     * Creates an instance of MetricsBatchResult.
     * @param metricsQueryResults the metrics results for individual queries
     */
    public MetricsQueryResourcesResult(List<MetricsQueryResult> metricsQueryResults) {
        this.metricsQueryResults = metricsQueryResults;
    }

    /**
     * Get the metrics results for individual queries.
     * @return returns the metrics results for individual queries
     */
    public List<MetricsQueryResult> getMetricsQueryResults() {
        return metricsQueryResults;
    }
}
