// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosEndToEndOperationLatencyPolicyConfig;

import java.time.Duration;

public class CosmosEndToEndOperationLatencyPolicyConfigBuilder {
    private final boolean isEnabled;
    private Duration endToEndOperationTimeout = Duration.ofSeconds(2);
    private Duration speculativeProcessingThreshold = null;
    private boolean isSpeculativeProcessingEnabled;


    public CosmosEndToEndOperationLatencyPolicyConfigBuilder() {
        this(true);
    }

    public CosmosEndToEndOperationLatencyPolicyConfigBuilder(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public CosmosEndToEndOperationLatencyPolicyConfig build() {
        return new CosmosEndToEndOperationLatencyPolicyConfig(isEnabled, endToEndOperationTimeout, speculativeProcessingThreshold, isSpeculativeProcessingEnabled);
    }

    public CosmosEndToEndOperationLatencyPolicyConfigBuilder endToEndOperationTimeout(
        Duration endToEndOperationTimeout) {

        this.endToEndOperationTimeout = endToEndOperationTimeout;
        return this;
    }

    public CosmosEndToEndOperationLatencyPolicyConfigBuilder speculativeProcessing(
        Duration speculativeProcessingThresholdDuration) {

        this.speculativeProcessingThreshold = speculativeProcessingThresholdDuration;
        return this;
    }

    public CosmosEndToEndOperationLatencyPolicyConfigBuilder enableSpeculativeProcessing(boolean enabled){
        this.isSpeculativeProcessingEnabled = enabled;
        return this;
    }
}
