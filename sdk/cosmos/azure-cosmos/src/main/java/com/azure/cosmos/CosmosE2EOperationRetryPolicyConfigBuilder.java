// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.OperationCancelledException;

import java.time.Duration;

/**
 * Builder for CosmosEndToEndOperationLatencyPolicyConfig
 */
public class CosmosE2EOperationRetryPolicyConfigBuilder {
    private boolean isEnabled = true;
    private final Duration endToEndOperationTimeout;

    /**
     * Create a builder for {@link CosmosE2EOperationRetryPolicyConfig} with end to end operation timeout
     * @param endToEndOperationTimeout the timeout for request cancellation in Duration. Setting very low timeouts
     *                                can cause the request to never succeed.
     */
    public CosmosE2EOperationRetryPolicyConfigBuilder(Duration endToEndOperationTimeout) {
        this.endToEndOperationTimeout = endToEndOperationTimeout;
    }

    /**
     * Builds {@link CosmosE2EOperationRetryPolicyConfig} with specified properties
     *
     * @return the {@link CosmosE2EOperationRetryPolicyConfig}
     */
    public CosmosE2EOperationRetryPolicyConfig build() {
        return new CosmosE2EOperationRetryPolicyConfig(isEnabled, endToEndOperationTimeout);
    }

    /**
     * Enables or disables the policy. It defaults to enable.
     * {@link OperationCancelledException}
     *

     * @return current CosmosEndToEndOperationConfigBuilder
     */
    public CosmosE2EOperationRetryPolicyConfigBuilder isEnabled(
        boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

}
