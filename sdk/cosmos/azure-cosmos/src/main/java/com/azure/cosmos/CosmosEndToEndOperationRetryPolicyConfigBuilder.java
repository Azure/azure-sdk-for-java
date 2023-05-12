// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.OperationCancelledException;

import java.time.Duration;

/**
 * Builder for CosmosEndToEndOperationLatencyPolicyConfig
 */
public class CosmosEndToEndOperationRetryPolicyConfigBuilder {
    private boolean isEnabled = true;
    private final Duration endToEndOperationTimeout;

    /**
     * Create a builder for {@link CosmosEndToEndOperationRetryPolicyConfig} with end to end operation timeout
     * @param endToEndOperationTimeout the timeout for request cancellation in Duration. Setting very low timeouts
     *                                can cause the request to never succeed.
     */
    public CosmosEndToEndOperationRetryPolicyConfigBuilder(Duration endToEndOperationTimeout) {
        this.endToEndOperationTimeout = endToEndOperationTimeout;
    }

    /**
     * Builds {@link CosmosEndToEndOperationRetryPolicyConfig} with specified properties
     *
     * @return the {@link CosmosEndToEndOperationRetryPolicyConfig}
     */
    public CosmosEndToEndOperationRetryPolicyConfig build() {
        return new CosmosEndToEndOperationRetryPolicyConfig(isEnabled, endToEndOperationTimeout);
    }

    /**
     * Enables or disables the policy. It defaults to enable.
     * {@link OperationCancelledException}
     *
     * @param isEnabled flag to enable or disable the policy
     * @return current CosmosEndToEndOperationConfigBuilder
     */
    public CosmosEndToEndOperationRetryPolicyConfigBuilder enable(
        boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

}
