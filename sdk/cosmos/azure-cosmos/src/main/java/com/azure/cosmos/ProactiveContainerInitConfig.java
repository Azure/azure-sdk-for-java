// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosContainerIdentity;

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
     *
     * <p>
     *     Proactive connection regions constitute those regions whose replicas have connections opened to prior
     *     to performing any workload on the container. This way the latency associated with opening connections
     *     does not impact the latency associated with performing workloads on the container. These connections are
     *     opened synchronously when the {@link CosmosClient}/{@link CosmosAsyncClient} is built.
     * </p>
     * <p>
     *     These proactive connection regions are a subset of the preferred regions configured through the {@link CosmosClientBuilder}. The first
     *     {@link ProactiveContainerInitConfig#getNumProactiveConnectionRegions()} read regions from preferred regions are picked. In this context a write-region could also be a read-region but not vice-versa.
     * </p>
     * <p>
     *     Consider a multi-master account with client configured with preferred regions - "US West" (write-region) and "US East" (write-region)
     *     <ul>
     *         <li>
     *              If the no. of proactive regions is set to two, connections to "US West" and "US East" are opened proactively.
     *         </li>
     *         <li>
     *             If the no. of proactive regions is set to one, connections to "US West" are opened proactively.
     *         </li>
     *     </ul>
     *     Consider a single-master account with client configured with preferred regions - "US West" (read-region), "US East" (read-region) and
     *     "West Europe" (write-region)
     *     <ul>
     *         <li>
     *              If the no. of proactive regions is set to two, connections to "US West" and "US East" are opened proactively. If your application
     *              has workloads which are write-heavy it is important to prioritize write regions in the list of preferred regions.
     *         </li>
     *     </ul>
     * </p>
     * */
    public int getNumProactiveConnectionRegions() {
        return numProactiveConnectionRegions;
    }
}
