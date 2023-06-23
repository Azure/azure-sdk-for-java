// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.directconnectivity.ContainerDirectConnectionMetadata;
import com.azure.cosmos.models.CosmosContainerIdentity;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * A builder to build {@link CosmosContainerProactiveInitConfig}
 * */
public final class CosmosContainerProactiveInitConfigBuilder {

    private static final int MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS = 5;
    private static final int MAX_MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT = 10;
    private final Map<CosmosContainerIdentity, ContainerDirectConnectionMetadata> containerDirectConnectionMetadataMap;
    private int numProactiveConnectionRegions;
    private Duration aggressiveWarmupDuration;

    /**
     * Instantiates {@link CosmosContainerProactiveInitConfigBuilder}
     *
     * @param cosmosContainerIdentities the container identities - this parameter must be non-empty
     */
    public CosmosContainerProactiveInitConfigBuilder(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        checkArgument(
            cosmosContainerIdentities != null && !cosmosContainerIdentities.isEmpty(),
            "The list of container identities cannot be null or empty.");
        this.containerDirectConnectionMetadataMap = new HashMap<>();

        if (cosmosContainerIdentities != null && !cosmosContainerIdentities.isEmpty()) {
            for (CosmosContainerIdentity cosmosContainerIdentity : cosmosContainerIdentities) {
                this.containerDirectConnectionMetadataMap.put(cosmosContainerIdentity, new ContainerDirectConnectionMetadata());
            }
        }

        this.numProactiveConnectionRegions = 1;
    }

    /**
     * Sets the no. of regions to proactively connect to.
     * <p>
     * <br>The no of proactive regions to connect to belong to the preferred list of regions
     * <br>In order to minimize latencies associated with warming up caches and opening connections
     * the no. of proactive connection regions cannot be more
     * than {@link CosmosContainerProactiveInitConfigBuilder#MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS}.
     * </p>
     *
     * @param numProactiveConnectionRegions the no of proactive connection regions
     * @return Current {@link CosmosContainerProactiveInitConfigBuilder}
     */
    public CosmosContainerProactiveInitConfigBuilder setProactiveConnectionRegionsCount(int numProactiveConnectionRegions) {
        checkArgument(
            numProactiveConnectionRegions > 0 &&

                numProactiveConnectionRegions <= MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS,
                    "The no. of regions to proactively connect to cannot be less than 1 or more than {}.",
                    MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS);
        this.numProactiveConnectionRegions = numProactiveConnectionRegions;
        return this;
    }

    /**
     * Sets the time window represented as a {@link Duration} within which connections will be opened
     * aggressively and in a blocking manner and outside which connections will be opened defensively
     * and in a non-blocking manner
     *
     * @param aggressiveWarmupDuration this denotes the aggressive proactive connection
     *                                                             establishment duration to be set
     * @return current {@link CosmosContainerProactiveInitConfigBuilder}
     */
    public CosmosContainerProactiveInitConfigBuilder setAggressiveWarmupDuration(Duration aggressiveWarmupDuration) {
        checkArgument(aggressiveWarmupDuration != null,
                "aggressiveWarmupDuration cannot be a null value");
        checkArgument(aggressiveWarmupDuration.compareTo(Duration.ZERO) > 0,
                "aggressiveWarmupDuration should be greater than Duration.ZERO");
        this.aggressiveWarmupDuration = aggressiveWarmupDuration;
        return this;
    }

    /**
     * Sets the minimum no. of connections required to be opened for each replica of
     * the container specified in the {@link CosmosContainerIdentity} instance.
     *
     * @param cosmosContainerIdentity Encapsulates the identity for the container for which minimum no. of connections
     *                                are to be opened.
     * @param minConnectionsPerEndpoint Denotes the minimum no. of connections to be opened for each endpoint of the container
     *
     * @return Current {@link CosmosContainerProactiveInitConfigBuilder}
     * */
    CosmosContainerProactiveInitConfigBuilder setMinConnectionPoolSizePerEndpointForContainer(CosmosContainerIdentity cosmosContainerIdentity, int minConnectionsPerEndpoint) {
        checkArgument(minConnectionsPerEndpoint >= 0 && minConnectionsPerEndpoint <= MAX_MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT,
                "minConnectionsPerEndpoint cannot be negative or greater than {}", MAX_MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT);

        ContainerDirectConnectionMetadata containerDirectConnectionMetadata = this.containerDirectConnectionMetadataMap.get(cosmosContainerIdentity);
        containerDirectConnectionMetadata.setMinConnectionPoolSizePerEndpointForContainer(minConnectionsPerEndpoint);

        return this;
    }

    /**
     * Builds {@link CosmosContainerProactiveInitConfig} with the provided properties
     *
     * @return an instance of {@link CosmosContainerProactiveInitConfig}
     * */
    public CosmosContainerProactiveInitConfig build() {
        checkArgument(
            numProactiveConnectionRegions >= 0 &&
                numProactiveConnectionRegions <= MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS,
                "The no. of regions to proactively connect to cannot be less than 0 or more than {}.",
                    MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS);
        return new CosmosContainerProactiveInitConfig(
                this.numProactiveConnectionRegions,
                this.containerDirectConnectionMetadataMap,
                this.aggressiveWarmupDuration
        );
    }
}
