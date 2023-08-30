// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;

import java.util.concurrent.atomic.AtomicInteger;

public final class ContainerDirectConnectionMetadata {

    private final AtomicInteger minConnectionPoolSizePerEndpointForContainer;

    // NOTE: should be compared with COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT
    // read during client initialization before connections are created for the container
    public ContainerDirectConnectionMetadata() {
        this.minConnectionPoolSizePerEndpointForContainer = new AtomicInteger(Configs.getMinConnectionPoolSizePerEndpoint());
    }

    public void setMinConnectionPoolSizePerEndpointForContainer(int minConnectionPoolSizePerEndpointForContainer) {
        this.minConnectionPoolSizePerEndpointForContainer.set(minConnectionPoolSizePerEndpointForContainer);
    }

    int getMinConnectionPoolSizePerEndpointForContainer() {
        return this.minConnectionPoolSizePerEndpointForContainer.get();
    }
}
