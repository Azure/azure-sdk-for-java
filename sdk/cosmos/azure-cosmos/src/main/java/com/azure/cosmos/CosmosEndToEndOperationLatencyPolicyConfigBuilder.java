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

    /**
     * Create a builder for {@link CosmosEndToEndOperationLatencyPolicyConfig} with end to end operation timeout
     * @param endToEndOperationTimeout the timeout for request cancellation in Duration. Setting very low timeouts
     *                                can cause the request to never succeed.
     */
    public CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration endToEndOperationTimeout) {
        this.endToEndOperationTimeout = endToEndOperationTimeout;
    }

    /**
     * Builds {@link CosmosEndToEndOperationLatencyPolicyConfig} with specified properties
     *
     * @return the {@link CosmosEndToEndOperationLatencyPolicyConfig}
     */
    public CosmosEndToEndOperationLatencyPolicyConfig build() {
        return new CosmosEndToEndOperationLatencyPolicyConfig(isEnabled, endToEndOperationTimeout);
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

}
