// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.models.CosmosContainerIdentity;

import java.util.concurrent.atomic.AtomicInteger;

public class ContainerDirectConnectionMetadata {
    private final CosmosContainerIdentity cosmosContainerIdentity;
    private final AtomicInteger minConnectionCountPerEndpoint;

    public ContainerDirectConnectionMetadata(CosmosContainerIdentity cosmosContainerIdentity) {
        this.cosmosContainerIdentity = cosmosContainerIdentity;
        this.minConnectionCountPerEndpoint = new AtomicInteger(1);
    }

    public void setMinConnectionCountPerEndpoint(int minConnectionCountPerEndpoint) {
        this.minConnectionCountPerEndpoint.set(minConnectionCountPerEndpoint);
    }

    int getMinConnectionCountPerEndpoint() {
        return this.minConnectionCountPerEndpoint.get();
    }
}
