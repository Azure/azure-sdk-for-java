// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;

import java.time.Duration;

/**
 * Represents End to end operation latency policy config
 * This enables requests to get cancelled by the client once the specified timeout is reached
 */
public class CosmosEndToEndOperationLatencyPolicyConfig {
    /**
     * The default end to end operation latency policy with timeout of 2 seconds
     */
    public static CosmosEndToEndOperationLatencyPolicyConfig DEFAULT =
        new CosmosEndToEndOperationLatencyPolicyConfigBuilder().build();

    /**
     * The disabled end to end operation latency policy
     */
    public static CosmosEndToEndOperationLatencyPolicyConfig DISABLED =
        new CosmosEndToEndOperationLatencyPolicyConfigBuilder(false).build();

    private final boolean isEnabled;  // Can be inferred from endToEndOperationTimeout not being null?
    private final Duration endToEndOperationTimeout; // No lowerbound?

    public CosmosEndToEndOperationLatencyPolicyConfig(
        boolean isEnabled,
        Duration endToEndOperationTimeout) {

        this.isEnabled = isEnabled;
        this.endToEndOperationTimeout = endToEndOperationTimeout;
    }

    /**
     * Returns if the policy is enabled or not
     *
     * @return if the policy is enabled or not
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Gets the defined end to end operatoin timeout
     *
     * @return the end to end operation timeout
     */
    public Duration getEndToEndOperationTimeout() {
        return endToEndOperationTimeout;
    }

}
