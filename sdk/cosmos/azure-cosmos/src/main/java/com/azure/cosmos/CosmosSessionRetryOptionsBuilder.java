// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * A {@link CosmosSessionRetryOptionsBuilder} instance will be used to build
 * a {@link CosmosSessionRetryOptions} instance.
 * */
public final class CosmosSessionRetryOptionsBuilder {

    private CosmosRegionSwitchHint regionSwitchHint;

    /**
     * Sets the {@link CosmosRegionSwitchHint} which specifies for
     * a request whether internal retry policies should prioritize a local region or a remote region.
     *
     * <p>
     * NOTES:
     * <ul>
     *     <li>{@code null} values are not allowed</li>
     * </ul>
     *
     * @param regionSwitchHint The region switch hint
     * @return This instance of {@link CosmosSessionRetryOptionsBuilder}
     * */
    public CosmosSessionRetryOptionsBuilder regionSwitchHint(CosmosRegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint = regionSwitchHint;
        return this;
    }

    /**
     * Builds an instance of {@link CosmosSessionRetryOptions}
     *
     * @return An instance of {@link CosmosSessionRetryOptions}
     * */
    public CosmosSessionRetryOptions build() {
        checkNotNull(regionSwitchHint, "regionSwitch hint cannot be null");
        return new CosmosSessionRetryOptions(regionSwitchHint);
    }
}
