// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

/**
 * Interface for tracking metrics associated with a specific operation e.g. GET, QUERY on a CosmosDB collection.
 */
public interface Metrics {

    /**
     * @param metricName metric for this operation e.g. CallCount, NotFound
     */
    void logCounterMetric(final String metricName);

    /**
     * Track successful completion of the operation
     *
     * @param startTimeInMillis The start time for the operation
     */
    void completed(final long startTimeInMillis);

    /**
     * Track operation errors
     *
     * @param startTimeInMillis The start time for the operation
     */
    void error(final long startTimeInMillis);
}
