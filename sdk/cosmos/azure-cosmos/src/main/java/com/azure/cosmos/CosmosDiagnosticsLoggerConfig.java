// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import java.time.Duration;

public final class CosmosDiagnosticsLoggerConfig {
    private Duration pointOperationLatencyThreshold;
    private Duration feedOperationLatencyThreshold;
    private float requestChargeThreshold;

    public CosmosDiagnosticsLoggerConfig() {
        this(Duration.ofSeconds(1), Duration.ofSeconds(3), 1000);
    }

    public CosmosDiagnosticsLoggerConfig(
        Duration pointOperationLatencyThreshold,
        Duration feedOperationLatencyThreshold,
        float requestChargeThreshold) {

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

    public float getRequestChargeThreshold() {
        return this.requestChargeThreshold;
    }

    public CosmosDiagnosticsLoggerConfig setRequestChargeThreshold(
        float newRequestChargeThreshold) {

        this.requestChargeThreshold = newRequestChargeThreshold;

        return this;
    }
}