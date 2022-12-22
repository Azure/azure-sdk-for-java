// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.time.Duration;

public class CosmosEndToEndOperationLatencyPolicyConfigBuilder {
    private final boolean isEnabled;
    private Duration endToEndOperationTimeout = Duration.ofSeconds(2);
    private Duration speculativeProcessingThreshold = null;

    public CosmosEndToEndOperationLatencyPolicyConfigBuilder() {
        this(true);
    }

    CosmosEndToEndOperationLatencyPolicyConfigBuilder(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public CosmosEndToEndOperationLatencyPolicyConfig build() {
        // TODO implement
        return null;
    }

    public CosmosEndToEndOperationLatencyPolicyConfigBuilder endToEndOperationTimeout(
        Duration endToEndOperationTimeout) {

        // TODO implement
        return null;
    }

    public CosmosEndToEndOperationLatencyPolicyConfigBuilder speculativeProcessing(
        Duration speculativeProcessingThresholdDuration) {

        // TODO implement
        return null;
    }
}
