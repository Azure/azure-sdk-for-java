// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import java.util.concurrent.atomic.AtomicInteger;

public final class ContainerDirectConnectionMetadata {

    private final AtomicInteger minConnectionPoolSizePerEndpointForContainer;

    public ContainerDirectConnectionMetadata() {
        this.minConnectionPoolSizePerEndpointForContainer = new AtomicInteger(1);
    }

    public void setMinConnectionPoolSizePerEndpointForContainer(int minConnectionPoolSizePerEndpointForContainer) {
        this.minConnectionPoolSizePerEndpointForContainer.set(minConnectionPoolSizePerEndpointForContainer);
    }

    int getMinConnectionPoolSizePerEndpointForContainer() {
        return this.minConnectionPoolSizePerEndpointForContainer.get();
    }
}
