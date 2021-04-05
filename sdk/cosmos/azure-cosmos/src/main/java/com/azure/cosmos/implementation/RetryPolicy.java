// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.ThrottlingRetryOptions;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 * <p>
 * Represents the retry policy configuration associated with a DocumentClient instance.
 */
public class RetryPolicy implements IRetryPolicyFactory {
    private final DiagnosticsClientContext diagnosticsClientContext;
    private final GlobalEndpointManager globalEndpointManager;
    private final boolean enableEndpointDiscovery;
    private final ThrottlingRetryOptions throttlingRetryOptions;
    private CosmosDiagnostics cosmosDiagnostics;
    private ClientRetryPolicy clientRetryPolicy;

    public RetryPolicy(DiagnosticsClientContext diagnosticsClientContext, GlobalEndpointManager globalEndpointManager
        , ConnectionPolicy connectionPolicy) {
        this.diagnosticsClientContext = diagnosticsClientContext;
        this.enableEndpointDiscovery = connectionPolicy.isEndpointDiscoveryEnabled();
        this.globalEndpointManager = globalEndpointManager;
        this.throttlingRetryOptions = connectionPolicy.getThrottlingRetryOptions();
    }

    @Override
    public DocumentClientRetryPolicy getRequestPolicy() {
        if (clientRetryPolicy == null) {
            clientRetryPolicy = new ClientRetryPolicy(this.diagnosticsClientContext,
                this.globalEndpointManager, this.enableEndpointDiscovery, this.throttlingRetryOptions);
            this.cosmosDiagnostics = clientRetryPolicy.getCosmosDiagnostics();
        }
        return clientRetryPolicy;
    }

    @Override
    public RetryContext getRetryContext() {
        return null;
    }
}
