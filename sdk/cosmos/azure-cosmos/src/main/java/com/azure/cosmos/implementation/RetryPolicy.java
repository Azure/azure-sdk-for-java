// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * Represents the retry policy configuration associated with a DocumentClient instance.
 */
public class RetryPolicy implements IRetryPolicyFactory {
    private final DiagnosticsClientContext diagnosticsClientContext;
    private final GlobalEndpointManager globalEndpointManager;
    private final GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker;
    private final GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover;
    private final boolean enableEndpointDiscovery;
    private final ThrottlingRetryOptions throttlingRetryOptions;
    private RxCollectionCache rxCollectionCache;

    public RetryPolicy(
        DiagnosticsClientContext diagnosticsClientContext,
        GlobalEndpointManager globalEndpointManager,
        ConnectionPolicy connectionPolicy,
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker,
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover) {

        this.diagnosticsClientContext = diagnosticsClientContext;
        this.enableEndpointDiscovery = connectionPolicy.isEndpointDiscoveryEnabled();
        this.globalEndpointManager = globalEndpointManager;
        this.throttlingRetryOptions = connectionPolicy.getThrottlingRetryOptions();
        this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker = globalPartitionEndpointManagerForPerPartitionCircuitBreaker;
        this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover = globalPartitionEndpointManagerForPerPartitionAutomaticFailover;
    }

    @Override
    public DocumentClientRetryPolicy getRequestPolicy(DiagnosticsClientContext clientContextOverride) {
        DiagnosticsClientContext effectiveClientContext = this.diagnosticsClientContext;
        if (clientContextOverride != null) {
            effectiveClientContext = clientContextOverride;
        }
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            effectiveClientContext,
            this.globalEndpointManager,
            this.enableEndpointDiscovery,
            this.throttlingRetryOptions,
            this.rxCollectionCache,
            this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker,
            this.globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

        return clientRetryPolicy;
    }

    @Override
    public RetryContext getRetryContext() {
        return null;
    }

    public void setRxCollectionCache(RxCollectionCache rxCollectionCache) {
        this.rxCollectionCache = rxCollectionCache;
    }
}
