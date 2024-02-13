// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import java.io.Serializable;
import java.time.Duration;

/**
 * Dedicated Gateway Request Options
 */
public final class DedicatedGatewayRequestOptions implements Serializable {

    /**
     * The staleness value associated with the request in the Azure CosmosDB service.
     */
    private Duration maxIntegratedCacheStaleness;

    /**
     * A flag indicating whether the integrated cache is enabled or bypassed with the request in Azure CosmosDB service.
     */
    private boolean integratedCacheBypassed;

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

    /**
     * Gets if the integrated cache is enabled or bypassed with the request in Azure CosmosDB service.
     *
     * <p>Default value is false</p>
     *
     * @return bypassIntegratedCache boolean value
     */
    public boolean isIntegratedCacheBypassed() {
        return integratedCacheBypassed;
    }

    /**
     * Sets if integrated cache should be enabled or bypassed for the request in Azure CosmosDB service.
     *
     * <p>Default value is false</p>
     *
     * @param bypassIntegratedCache boolean value
     * @return this DedicatedGatewayRequestOptions
     */
    public DedicatedGatewayRequestOptions setIntegratedCacheBypassed(boolean bypassIntegratedCache) {
        this.integratedCacheBypassed = bypassIntegratedCache;
        return this;
    }
}
