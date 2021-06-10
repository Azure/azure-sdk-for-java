// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

import java.time.Duration;

/**
 * Dedicated Gateway Request Options
 */
@Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class DedicatedGatewayRequestOptions {

    private Duration maxIntegratedCacheStaleness;

    /**
     * Constructor
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public DedicatedGatewayRequestOptions() {

    }

    /**
     * Gets the staleness value associated with the request in the Azure CosmosDB service. For requests where the {@link
     * com.azure.cosmos.ConsistencyLevel} is {@link com.azure.cosmos.ConsistencyLevel#EVENTUAL}, responses from the
     * integrated cache are guaranteed to be no staler than value indicated by this maxIntegratedCacheStaleness.
     *
     * <p>Default value is null</p>
     *
     * <p>Cache Staleness is supported in milliseconds granularity. Anything smaller than milliseconds will be ignored.</p>
     *
     * @return Duration of maxIntegratedCacheStaleness
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getMaxIntegratedCacheStaleness() {
        return maxIntegratedCacheStaleness;
    }

    /**
     * Sets the staleness value associated with the request in the Azure CosmosDB service. For requests where the {@link
     * com.azure.cosmos.ConsistencyLevel} is {@link com.azure.cosmos.ConsistencyLevel#EVENTUAL}, responses from the
     * integrated cache are guaranteed to be no staler than value indicated by this maxIntegratedCacheStaleness.
     *
     * <p>Default value is null</p>
     *
     * <p>Cache Staleness is supported in milliseconds granularity. Anything smaller than milliseconds will be ignored.</p>
     *
     * @param maxIntegratedCacheStaleness Max Integrated Cache Staleness duration
     * @return this DedicatedGatewayRequestOptions
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public DedicatedGatewayRequestOptions setMaxIntegratedCacheStaleness(Duration maxIntegratedCacheStaleness) {
        this.maxIntegratedCacheStaleness = maxIntegratedCacheStaleness;
        return this;
    }
}
