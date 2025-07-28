package com.azure.monitor.query.metrics.models;

import java.util.List;

public class MetricsQueryResourcesResult {
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
