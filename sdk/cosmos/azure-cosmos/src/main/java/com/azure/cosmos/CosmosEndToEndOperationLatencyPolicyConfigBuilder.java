// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.OperationCancelledException;

import java.time.Duration;

/**
 * Builder for CosmosEndToEndOperationLatencyPolicyConfig
 */
public class CosmosEndToEndOperationLatencyPolicyConfigBuilder {
    private boolean isEnabled = true;
    private final Duration endToEndOperationTimeout;
    private Duration endToEndFeedOperationTimeout;
    private AvailabilityStrategy availabilityStrategy;

    /**
     * Create a builder for {@link CosmosEndToEndOperationLatencyPolicyConfig} with end to end operation timeout
     * @param endToEndOperationTimeout the timeout for request cancellation in Duration. Setting very low timeouts
     *                                can cause the request to never succeed.
     */
    public CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration endToEndOperationTimeout) {
        this.endToEndOperationTimeout = endToEndOperationTimeout;
        endToEndFeedOperationTimeout = endToEndOperationTimeout;
    }

    /**
     * Builds {@link CosmosEndToEndOperationLatencyPolicyConfig} with specified properties
     *
     * @return the {@link CosmosEndToEndOperationLatencyPolicyConfig}
     */
    public CosmosEndToEndOperationLatencyPolicyConfig build() {
        return new CosmosEndToEndOperationLatencyPolicyConfig(isEnabled, endToEndOperationTimeout, endToEndFeedOperationTimeout, availabilityStrategy);
    }

    /**
     * Enables or disables the policy. It defaults to enable.
     * {@link OperationCancelledException}
     *
     * @param isEnabled flag to enable or disable the policy
     * @return current CosmosEndToEndOperationConfigBuilder
     */
    public CosmosEndToEndOperationLatencyPolicyConfigBuilder enable(
        boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    /**
     * Sets the availability strategy to be used for the policy.
     *
     * @param availabilityStrategy the availability strategy to be used for the policy
     * @return current CosmosEndToEndOperationConfigBuilder
     */
    public CosmosEndToEndOperationLatencyPolicyConfigBuilder setAvailabilityStrategy(
        AvailabilityStrategy availabilityStrategy) {
        this.availabilityStrategy = availabilityStrategy;
        return this;
    }

    public Duration getEndToEndFeedOperationTimeout() {
        return endToEndFeedOperationTimeout;
    }

    /**
     * Sets the feed operation timeout. Defaults to endToEndOperationTimeout. Use this if you need to set a different timeout
     * for feed operations
     * @param endToEndFeedOperationTimeout feed operatoin timeout Duration
     * @return current CosmosEndToEndOperationConfigBuilder
     */
    public CosmosEndToEndOperationLatencyPolicyConfigBuilder setEndToEndFeedOperationTimeout(Duration endToEndFeedOperationTimeout) {
        this.endToEndFeedOperationTimeout = endToEndFeedOperationTimeout;
        return this;
    }
}
