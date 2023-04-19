// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosEndToEndOperationLatencyPolicyConfig;

import java.time.Duration;

/**
 * Builder for CosmosEndToEndOperationLatencyPolicyConfig
 */
public class CosmosEndToEndOperationLatencyPolicyConfigBuilder {
    private final boolean isEnabled;
    private Duration endToEndOperationTimeout = Duration.ofSeconds(2); //defaults to two seconds

    /**
     * Create a CosmosEndToEndOperationLatencyPolicyConfigBuilder enabling the policy
     */
    public CosmosEndToEndOperationLatencyPolicyConfigBuilder() {
        this(true);
    }

    /**
     * Enable or disable the end to end timeout policy
     * @param isEnabled Flag to toggle the end to end timeout policy
     */
    public CosmosEndToEndOperationLatencyPolicyConfigBuilder(boolean isEnabled) {
        this.isEnabled = isEnabled;
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
     * Specifies the endToEndOperationTimeout after which the request will be cancelled with
     * {@link com.azure.cosmos.implementation.RequestCancelledException}
     *
     * @param endToEndOperationTimeout the timeout for request cancellation in Duration. Setting very low timeouts
     *                                can cause the request to never succeed.
     * @return current CosmosEndToEndOperationLatencyPolicyConfigBuilder
     */
    public CosmosEndToEndOperationLatencyPolicyConfigBuilder endToEndOperationTimeout(
        Duration endToEndOperationTimeout) {

        this.endToEndOperationTimeout = endToEndOperationTimeout;
        return this;
    }

}
