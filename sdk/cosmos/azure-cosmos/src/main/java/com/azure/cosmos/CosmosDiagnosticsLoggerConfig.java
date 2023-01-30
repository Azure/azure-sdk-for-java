// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import java.time.Duration;

/**
 * Configuration options that determine how the Cosmos diagnostics logger decides whether to log diagnostics
 * for an operation or not
 */
public final class CosmosDiagnosticsLoggerConfig {
    private Duration pointOperationLatencyThreshold;
    private Duration feedOperationLatencyThreshold;
    private float requestChargeThreshold;

    /**
     * Creates an instance of the CosmosDiagnosticsLoggerConfig class with default parameters
     */
    public CosmosDiagnosticsLoggerConfig() {
        this(Duration.ofSeconds(1), Duration.ofSeconds(3), 1000);
    }

    /**
     * Creates an instance of the CosmosDiagnosticsLoggerConfig class with custom parameters
     * @param pointOperationLatencyThreshold the latency threshold for all point operations
     * @param feedOperationLatencyThreshold the latency threshold for feed operations (query and change feed - and bulk)
     * @param requestChargeThreshold the request charge threshold
     */
    public CosmosDiagnosticsLoggerConfig(
        Duration pointOperationLatencyThreshold,
        Duration feedOperationLatencyThreshold,
        float requestChargeThreshold) {

        this.pointOperationLatencyThreshold = pointOperationLatencyThreshold;
        this.feedOperationLatencyThreshold = feedOperationLatencyThreshold;
        this.requestChargeThreshold = requestChargeThreshold;
    }

    /**
     * Diagnostics for point operations will be logged if their end-to-end latency exceed this value
     * @return the end-to-end latency threshold for point operations
     */
    public Duration getPointOperationLatencyThreshold() {
        return this.pointOperationLatencyThreshold;
    }

    /**
     * Diagnostics for point operations will be logged if their end-to-end latency exceed this value
     * @param newPointOperationLatencyThreshold The end-to-end latency threshold for point operations
     * @return the CosmosDiagnosticsLoggerConfig instance
     */
    public CosmosDiagnosticsLoggerConfig setPointOperationLatencyThreshold(
        Duration newPointOperationLatencyThreshold) {

        this.pointOperationLatencyThreshold = newPointOperationLatencyThreshold;

        return this;
    }

    /**
     * Diagnostics for feed operations will be logged if their end-to-end latency exceed this value
     * @return the end-to-end latency threshold for feed operations
     */
    public Duration getFeedOperationLatencyThreshold() {
        return this.feedOperationLatencyThreshold;
    }

    /**
     * Diagnostics for feed operations will be logged if their end-to-end latency exceed this value
     * @param newFeedOperationLatencyThreshold The end-to-end latency threshold for feed operations
     * @return the CosmosDiagnosticsLoggerConfig instance
     */
    public CosmosDiagnosticsLoggerConfig setFeedOperationLatencyThreshold(
        Duration newFeedOperationLatencyThreshold) {

        this.feedOperationLatencyThreshold = newFeedOperationLatencyThreshold;

        return this;
    }

    /**
     * Diagnostics for operations will be logged if their total request charge exceeds this threshold
     * @return the total request charge threshold
     */
    public float getRequestChargeThreshold() {
        return this.requestChargeThreshold;
    }

    /**
     * Diagnostics for operations will be logged if their total request charge exceeds this threshold
     * @param newRequestChargeThreshold the total request charge threshold
     * @return the CosmosDiagnosticsLoggerConfig instance
     */
    public CosmosDiagnosticsLoggerConfig setRequestChargeThreshold(
        float newRequestChargeThreshold) {

        this.requestChargeThreshold = newRequestChargeThreshold;

        return this;
    }
}