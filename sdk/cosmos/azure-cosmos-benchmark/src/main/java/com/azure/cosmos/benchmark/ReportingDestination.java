// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

/**
 * The destination for detailed benchmark metrics reporting.
 * Console logging via {@code LoggingMeterRegistry} is always active alongside
 * any configured destination.
 */
public enum ReportingDestination {
    /**
     * Per-metric CSV files via CsvMetricsReporter (Micrometer-based).
     */
    CSV,

    /**
     * Upload all metrics with full dimensions to a Cosmos DB container.
     */
    COSMOSDB,

    /**
     * Send all metrics to Azure Application Insights via AzureMonitorMeterRegistry.
     */
    APPLICATION_INSIGHTS
}
