// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosContainerIdentity;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encapsulates the list of container identities and no. of proactive connection regions.
 * */
public final class CosmosContainerProactiveInitConfig {
    private final static ImplementationBridgeHelpers.CosmosContainerIdentityHelper.CosmosContainerIdentityAccessor
        containerIdAccessor = ImplementationBridgeHelpers
            .CosmosContainerIdentityHelper
            .getCosmosContainerIdentityAccessor();
    private final List<CosmosContainerIdentity> cosmosContainerIdentities;
    private final Map<String, Integer> containerLinkToMinConnectionsMap;
    private final int numProactiveConnectionRegions;
    private final Duration aggressiveWarmupDuration;

    CosmosContainerProactiveInitConfig(
        List<CosmosContainerIdentity> cosmosContainerIdentities,
        int numProactiveConnectionRegions,
        Map<String, Integer> containerLinkToMinConnectionsMap,
        Duration aggressiveWarmupDuration
    ) {
        this.cosmosContainerIdentities = Collections.unmodifiableList(cosmosContainerIdentities);
        this.numProactiveConnectionRegions = numProactiveConnectionRegions;
        this.containerLinkToMinConnectionsMap = containerLinkToMinConnectionsMap;
        this.aggressiveWarmupDuration = aggressiveWarmupDuration;
    }

    /**
     * Gets the list of container identities. The returned list is protected against modifications.
     *
     * @return list of {@link CosmosContainerIdentity}
     * */
    public List<CosmosContainerIdentity> getCosmosContainerIdentities() {
        return cosmosContainerIdentities;
    }

    /**
     * Gets the no. of proactive connection regions
     *
     * <p>
     * Proactive connection regions constitute those regions where replicas of container partitions have connections opened to prior
     * to performing any workload on the container. This way the latency associated with opening connections
     * does not impact the latency associated with performing workloads on the container. These connections are
     * opened synchronously when the {@link CosmosClient}/{@link CosmosAsyncClient} is built.
     * </p>
     * <p>
     * These proactive connection regions are a subset of the preferred regions configured through the {@link CosmosClientBuilder}. The first
     * {@link CosmosContainerProactiveInitConfig#getProactiveConnectionRegionsCount()} read regions from preferred regions are picked. In this context a write-region could also be a read-region but not vice-versa.
     * </p>
     * <p>
     * Consider a multi-master account with client configured with preferred regions - "US West" (write-region) and "US East" (write-region)
     * <br>
     * 1. If the no. of proactive regions is set to two, connections to "US West" and "US East" are opened proactively.
     * <br>
     * 2. If the no. of proactive regions is set to one, connections to "US West" are opened proactively.
     * <br><br>
     * Consider a single-master account with client configured with preferred regions - "US West" (read-region), "US East" (read-region) and
     * "West Europe" (write-region)
     * <br>
     * 1. If the no. of proactive regions is set to two, connections to "US West" and "US East" are opened proactively. If your application
     * has workloads which are write-heavy it is important to prioritize write regions in the list of preferred regions.
     * </p>
     *
     * @return no. of proactive connection regions
     */
    public int getProactiveConnectionRegionsCount() {
        return numProactiveConnectionRegions;
    }

    /**
     * Gets the duration within which connections will be opened aggressively and in a blocking manner and outside
     * which connections will be opened defensively and in a non-blocking manner
     *
     * @return the aggressive proactive connection establishment duration
     * */
    Duration getAggressiveWarmupDuration() {
        return this.aggressiveWarmupDuration;
    }

    @Override
    public String toString() {

        if (this.cosmosContainerIdentities == null || cosmosContainerIdentities.isEmpty()) {
            return "";
        }

        return
            String.format(
                "%s(%d)",
                cosmosContainerIdentities
                    .stream()
                    .map(ci -> String.join(
                        ".",
                        containerIdAccessor.getDatabaseName(ci),
                        containerIdAccessor.getContainerName(ci)))
                    .collect(Collectors.joining(",")),
                numProactiveConnectionRegions);
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosContainerProactiveInitConfigHelper.setCosmosContainerProactiveInitConfigAccessor(new ImplementationBridgeHelpers.CosmosContainerProactiveInitConfigHelper.CosmosContainerProactiveInitConfigAccessor() {
            @Override
            public Map<String, Integer> getContainerLinkToMinConnectionsMap(CosmosContainerProactiveInitConfig cosmosContainerProactiveInitConfig) {
                return cosmosContainerProactiveInitConfig.containerLinkToMinConnectionsMap;
            }
        });
    }

    static { initialize(); }
}
