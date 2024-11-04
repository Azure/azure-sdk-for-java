// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.circuitBreaker.GlobalPartitionEndpointManagerForCircuitBreaker;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * Represents the retry policy configuration associated with a DocumentClient instance.
 */
public class RetryPolicy implements IRetryPolicyFactory {
    private final DiagnosticsClientContext diagnosticsClientContext;
    private final GlobalEndpointManager globalEndpointManager;
    private final GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManager;
    private final boolean enableEndpointDiscovery;
    private final ThrottlingRetryOptions throttlingRetryOptions;
    private RxCollectionCache rxCollectionCache;

    public RetryPolicy(
        DiagnosticsClientContext diagnosticsClientContext,
        GlobalEndpointManager globalEndpointManager,
        ConnectionPolicy connectionPolicy,
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManager) {

        this.diagnosticsClientContext = diagnosticsClientContext;
        this.enableEndpointDiscovery = connectionPolicy.isEndpointDiscoveryEnabled();
        this.globalEndpointManager = globalEndpointManager;
        this.throttlingRetryOptions = connectionPolicy.getThrottlingRetryOptions();
        this.globalPartitionEndpointManager = globalPartitionEndpointManager;
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
            this.globalPartitionEndpointManager);

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
