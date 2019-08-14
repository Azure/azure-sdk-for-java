// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.RetryOptions;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 * 
 * Represents the retry policy configuration associated with a DocumentClient instance.
 */
public class RetryPolicy implements IRetryPolicyFactory {
    private final GlobalEndpointManager globalEndpointManager;
    private final boolean enableEndpointDiscovery;
    private final RetryOptions retryOptions;
    
    public RetryPolicy(GlobalEndpointManager globalEndpointManager, ConnectionPolicy connectionPolicy) {
        this.enableEndpointDiscovery = connectionPolicy.enableEndpointDiscovery();
        this.globalEndpointManager = globalEndpointManager;
        this.retryOptions = connectionPolicy.retryOptions();
    }

    @Override
    public IDocumentClientRetryPolicy getRequestPolicy() {
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(this.globalEndpointManager,
                this.enableEndpointDiscovery, this.retryOptions);

        return clientRetryPolicy;
    }
}
