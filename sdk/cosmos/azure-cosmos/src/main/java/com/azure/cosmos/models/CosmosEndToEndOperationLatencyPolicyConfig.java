// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;

import java.time.Duration;

public class CosmosEndToEndOperationLatencyPolicyConfig {
    public static CosmosEndToEndOperationLatencyPolicyConfig DEFAULT =
        new CosmosEndToEndOperationLatencyPolicyConfigBuilder().build();

    public static CosmosEndToEndOperationLatencyPolicyConfig DISABLED =
        new CosmosEndToEndOperationLatencyPolicyConfigBuilder(false).build();

    private final boolean isEnabled;
    private final Duration endToEndOperationTimeout;
    private final Duration speculativeProcessingThreshold;
    private final boolean isSpeculativeProcessingEnabled;

    public CosmosEndToEndOperationLatencyPolicyConfig(
        boolean isEnabled,
        Duration endToEndOperationTimeout,
        Duration speculativeProcessingThreshold,
        boolean isSpeculativeProcessingEnabled) {

        this.isEnabled = isEnabled;
        this.endToEndOperationTimeout = endToEndOperationTimeout;
        this.speculativeProcessingThreshold = speculativeProcessingThreshold;
        this.isSpeculativeProcessingEnabled = isSpeculativeProcessingEnabled;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public Duration getEndToEndOperationTimeout() {
        return endToEndOperationTimeout;
    }

    public Duration getSpeculativeProcessingThreshold() {
        return this.speculativeProcessingThreshold;
    }

}
