// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;

/**
 * Builder for CosmosEndToEndOperationLatencyPolicyConfig
 */
public class CosmosEndToEndOperationConfigBuilder {
    private final boolean isEnabled;
    private Duration endToEndOperationTimeout = Duration.ofSeconds(2); //defaults to two seconds

    /**
     * Create a CosmosEndToEndOperationConfigBuilder enabling the policy
     */
    public CosmosEndToEndOperationConfigBuilder() {
        this(true);
    }

    /**
     * Enable or disable the end to end timeout policy
     * @param isEnabled Flag to toggle the end to end timeout policy
     */
    public CosmosEndToEndOperationConfigBuilder(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * Builds {@link CosmosEndToEndOperationConfig} with specified properties
     *
     * @return the {@link CosmosEndToEndOperationConfig}
     */
    public CosmosEndToEndOperationConfig build() {
        return new CosmosEndToEndOperationConfig(isEnabled, endToEndOperationTimeout);
    }

    /**
     * Specifies the endToEndOperationTimeout after which the request will be cancelled with
     * {@link com.azure.cosmos.implementation.RequestCancelledException}
     *
     * @param endToEndOperationTimeout the timeout for request cancellation in Duration. Setting very low timeouts
     *                                can cause the request to never succeed.
     * @return current CosmosEndToEndOperationConfigBuilder
     */
    public CosmosEndToEndOperationConfigBuilder endToEndOperationTimeout(
        Duration endToEndOperationTimeout) {

        this.endToEndOperationTimeout = endToEndOperationTimeout;
        return this;
    }

}
