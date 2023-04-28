// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;

/**
 * Represents End to end operation latency policy config
 * This enables requests to get cancelled by the client once the specified timeout is reached
 */
public final class CosmosEndToEndOperationConfig {
    /**
     * The default end to end operation latency policy with timeout of 2 seconds
     */
    public static final CosmosEndToEndOperationConfig DEFAULT =
        new CosmosEndToEndOperationConfigBuilder().build();

    /**
     * The disabled end to end operation latency policy
     */
    public static final CosmosEndToEndOperationConfig DISABLED =
        new CosmosEndToEndOperationConfigBuilder(false).build();

    private final boolean isEnabled;
    private final Duration endToEndOperationTimeout;

    /**
     * Constructor
     * @param isEnabled toggle if the policy should be enabled or disabled
     * @param endToEndOperationTimeout the timeout for request cancellation in {@link Duration}. Setting very low timeouts
     *                                 can cause the request to never succeed.
     */
    CosmosEndToEndOperationConfig(
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
