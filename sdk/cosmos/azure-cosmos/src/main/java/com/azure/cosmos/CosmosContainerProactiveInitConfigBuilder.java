// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosContainerIdentity;

import java.util.List;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * A builder to build {@link CosmosContainerProactiveInitConfig}
 * */
public final class CosmosContainerProactiveInitConfigBuilder {

    private static final int MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS = 5;
    private final List<CosmosContainerIdentity> cosmosContainerIdentities;
    private int numProactiveConnectionRegions;

    /**
     * Instantiates {@link CosmosContainerProactiveInitConfigBuilder}
     *
     * @param cosmosContainerIdentities the container identities - this parameter must be non-empty
     */
    public CosmosContainerProactiveInitConfigBuilder(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        checkArgument(
            cosmosContainerIdentities != null && !cosmosContainerIdentities.isEmpty(),
            "The list of container identities cannot be null or empty.");
        this.cosmosContainerIdentities = cosmosContainerIdentities;
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
                this.cosmosContainerIdentities,
                this.numProactiveConnectionRegions
        );
    }
}
