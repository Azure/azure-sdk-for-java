// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import java.time.Duration;

/**
 * Dedicated Gateway Request Options
 */
public final class DedicatedGatewayRequestOptions {

    private Duration maxIntegratedCacheStaleness;

    /**
     * Constructor
     */
    public DedicatedGatewayRequestOptions() {

    }

    /**
     * Gets the staleness value associated with the request in the Azure CosmosDB service. For requests where the {@link
     * com.azure.cosmos.ConsistencyLevel} is {@link com.azure.cosmos.ConsistencyLevel#EVENTUAL} or {@link com.azure.cosmos.ConsistencyLevel#SESSION}, responses from the
     * integrated cache are guaranteed to be no staler than value indicated by this maxIntegratedCacheStaleness.
     *
     * <p>Default value is null</p>
     *
     * <p>Cache Staleness is supported in milliseconds granularity. Anything smaller than milliseconds will be ignored.</p>
     *
     * @return Duration of maxIntegratedCacheStaleness
     */
    public Duration getMaxIntegratedCacheStaleness() {
        return maxIntegratedCacheStaleness;
    }

    /**
     * Sets the staleness value associated with the request in the Azure CosmosDB service. For requests where the {@link
     * com.azure.cosmos.ConsistencyLevel} is {@link com.azure.cosmos.ConsistencyLevel#EVENTUAL} or {@link com.azure.cosmos.ConsistencyLevel#SESSION}, responses from the
     * integrated cache are guaranteed to be no staler than value indicated by this maxIntegratedCacheStaleness.
     *
     * <p>Default value is null</p>
     *
     * <p>Cache Staleness is supported in milliseconds granularity. Anything smaller than milliseconds will be ignored.</p>
     *
     * @param maxIntegratedCacheStaleness Max Integrated Cache Staleness duration
     * @return this DedicatedGatewayRequestOptions
     */
    public DedicatedGatewayRequestOptions setMaxIntegratedCacheStaleness(Duration maxIntegratedCacheStaleness) {
        this.maxIntegratedCacheStaleness = maxIntegratedCacheStaleness;
        return this;
    }
}
