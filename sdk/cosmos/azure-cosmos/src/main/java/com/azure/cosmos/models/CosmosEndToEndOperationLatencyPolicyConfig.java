// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;

import java.time.Duration;

/**
 * This may increase cost and decrease availability
 * May provide advantage only with multi region accounts
 */
public class CosmosEndToEndOperationLatencyPolicyConfig {
    public static CosmosEndToEndOperationLatencyPolicyConfig DEFAULT =
        new CosmosEndToEndOperationLatencyPolicyConfigBuilder().build();

    public static CosmosEndToEndOperationLatencyPolicyConfig DISABLED =
        new CosmosEndToEndOperationLatencyPolicyConfigBuilder(false).build();

    private final boolean isEnabled;  // Can be inferred from endToEndOperationTimeout not being null?
    private final Duration endToEndOperationTimeout; // No lowerbound?
    private final Duration speculativeProcessingThreshold;  // time at which sepculative processing can kick in
        // see if we can take percentage
    private final boolean isSpeculativeProcessingEnabled; // May not be required. Can be inferred from speculativeProcessingThreshold == null?

    /* TODO
        Things that can be controlled
        - Percentage of exploration requests
        - Warm up time Late Init? After collecting some data and building some context to avoid initial noise
        - Bias towards certain region (local) ?
        - **When the policy is specified at the operation level, it should override this?

     */


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
