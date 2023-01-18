// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosContainerIdentity;

import java.util.List;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * A builder to build {@link ProactiveContainerInitConfig}
 * */
public final class ProactiveContainerInitConfigBuilder {

    private static final int MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS = 5;
    private final List<CosmosContainerIdentity> cosmosContainerIdentities;
    private int numProactiveConnectionRegions;

    /**
     * Instantiates {@link ProactiveContainerInitConfigBuilder}
     *
     * @param cosmosContainerIdentities the container identities - this parameter must be non-empty
     */
    public ProactiveContainerInitConfigBuilder(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        checkArgument(cosmosContainerIdentities != null && !cosmosContainerIdentities.isEmpty(), "The list of container identities cannot be null or empty.");
        this.cosmosContainerIdentities = cosmosContainerIdentities;
    }

    /**
     * Sets the no. of regions to proactively connect to.
     * <p>
     * <br>The no of proactive regions to connect to belong to the preferred list of regions
     * <br>In order to minimize latencies associated with warming up caches and opening connections
     * the no. of proactive connection regions cannot be more than {@link ProactiveContainerInitConfigBuilder#MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS}.
     * </p>
     *
     * @param numProactiveConnectionRegions the no of proactive connection regions
     * @return Current {@link ProactiveContainerInitConfigBuilder}
     */
    public ProactiveContainerInitConfigBuilder setProactiveConnectionRegions(int numProactiveConnectionRegions) {
        checkArgument(numProactiveConnectionRegions >= 0 && numProactiveConnectionRegions <= MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS,
                String.format("The no. of regions to proactively connect to cannot be less than 0 or more than %d.", MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS));
        this.numProactiveConnectionRegions = numProactiveConnectionRegions;
        return this;
    }

    /**
     * Builds {@link ProactiveContainerInitConfig} with the provided properties
     *
     * @return an instance of {@link ProactiveContainerInitConfig}
     * */
    public ProactiveContainerInitConfig build() {
        checkArgument(numProactiveConnectionRegions >= 0 && numProactiveConnectionRegions <= MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS,
                String.format("The no. of regions to proactively connect to cannot be less than 0 or more than %d.", MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS));
        return new ProactiveContainerInitConfig(
                this.cosmosContainerIdentities,
                this.numProactiveConnectionRegions
        );
    }
}
