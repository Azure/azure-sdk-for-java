// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.models.CosmosContainerIdentity;

import java.util.concurrent.atomic.AtomicInteger;

public class ContainerDirectConnectionMetadata {
    private final CosmosContainerIdentity cosmosContainerIdentity;
    private final AtomicInteger minConnectionPoolSizePerEndpointForContainer;

    public ContainerDirectConnectionMetadata(CosmosContainerIdentity cosmosContainerIdentity) {
        this.cosmosContainerIdentity = cosmosContainerIdentity;
        this.minConnectionPoolSizePerEndpointForContainer = new AtomicInteger(1);
    }

    public void setMinConnectionPoolSizePerEndpointForContainer(int minConnectionPoolSizePerEndpointForContainer) {
        this.minConnectionPoolSizePerEndpointForContainer.set(minConnectionPoolSizePerEndpointForContainer);
    }

    int getMinConnectionPoolSizePerEndpointForContainer() {
        return this.minConnectionPoolSizePerEndpointForContainer.get();
    }
}
