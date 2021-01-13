package com.azure.cosmos.benchmark.linkedin.impl;

/**
 * Interface for tracking metrics associated with a specific operation e.g. GET, QUERY on a CosmosDB collection.
 */
public interface Metrics {

    /**
     * @param metricName metric for this operation e.g. Success, ErrorCount
     */
    void logCounterMetric(final String metricName);

    /**
     * Mark the completion of the operation
     *
     * @param startTimeInMillis The start time for the operation
     */
    void completed(final long startTimeInMillis);
}
