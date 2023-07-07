package com.azure.monitor.query.models;

import java.util.List;

/**
 * The result of a metrics query batch. It contains the results of individual queries.
 */
public class MetricsBatchResult {
    private List<MetricsQueryResult> metricsQueryResults;

    /**
     * Creates an instance of MetricsBatchResult.
     * @param metricsQueryResults the metrics results for individual queries
     */
    public MetricsBatchResult(List<MetricsQueryResult> metricsQueryResults) {
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
