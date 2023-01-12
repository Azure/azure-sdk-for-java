// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.util.List;

/**
 * Encapsulates the list of container identities and no. of proactive connection regions.
 * */
public final class ProactiveContainerInitConfig {
    private final List<CosmosContainerIdentity> cosmosContainerIdentities;
    private final int numProactiveConnectionRegions;

    ProactiveContainerInitConfig(List<CosmosContainerIdentity> cosmosContainerIdentities, int numProactiveConnectionRegions) {
        this.cosmosContainerIdentities = cosmosContainerIdentities;
        this.numProactiveConnectionRegions = numProactiveConnectionRegions;
    }

    /**
     * Gets the list of container identities
     * */
    public List<CosmosContainerIdentity> getCosmosContainerIdentities() {
        return cosmosContainerIdentities;
    }

    /**
     * Gets the no. of proactive connection regions
     * */
    public int getNumProactiveConnectionRegions() {
        return numProactiveConnectionRegions;
    }
}
