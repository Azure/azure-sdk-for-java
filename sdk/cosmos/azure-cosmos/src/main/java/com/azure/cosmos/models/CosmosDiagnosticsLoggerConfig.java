// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.CosmosDiagnostics;

import java.time.Duration;

public class CosmosDiagnosticsLoggerConfig {
    private Duration pointOperationLatencyThreshold;
    private Duration feedOperationLatencyThreshold;
    private double requestChargeThreshold;

    public CosmosDiagnosticsLoggerConfig() {
        this(Duration.ofSeconds(1), Duration.ofSeconds(3), 1000);
    }

    public CosmosDiagnosticsLoggerConfig(
        Duration pointOperationLatencyThreshold,
        Duration feedOperationLatencyThreshold,
        double requestChargeThreshold) {

        this.pointOperationLatencyThreshold = pointOperationLatencyThreshold;
        this.feedOperationLatencyThreshold = feedOperationLatencyThreshold;
        this.requestChargeThreshold = requestChargeThreshold;
    }

    public Duration getPointOperationLatencyThreshold() {
        return this.pointOperationLatencyThreshold;
    }

    public CosmosDiagnosticsLoggerConfig setPointOperationLatencyThreshold(
        Duration newPointOperationLatencyThreshold) {

        this.pointOperationLatencyThreshold = newPointOperationLatencyThreshold;

        return this;
    }

    public Duration getFeedOperationLatencyThreshold() {
        return this.feedOperationLatencyThreshold;
    }

    public CosmosDiagnosticsLoggerConfig setFeedOperationLatencyThreshold(
        Duration newFeedOperationLatencyThreshold) {

        this.feedOperationLatencyThreshold = newFeedOperationLatencyThreshold;

        return this;
    }

    public double getRequestChargeThreshold() {
        return this.requestChargeThreshold;
    }

    public CosmosDiagnosticsLoggerConfig setRequestChargeThreshold(
        double newRequestChargeThreshold) {

        this.requestChargeThreshold = newRequestChargeThreshold;

        return this;
    }
}
