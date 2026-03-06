// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

/**
 * The destination for detailed benchmark metrics reporting.
 * Console summary is always enabled regardless of this setting.
 */
public enum ReportingDestination {
    /**
     * Console summary only (no detailed metrics export).
     */
    CONSOLE,

    /**
     * Per-metric CSV files via Dropwizard CsvReporter.
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
