// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosRegionSwitchHint;

/**
 * A {@link CosmosSessionRetryOptionsBuilder} instance will be used to build
 * a {@link CosmosSessionRetryOptions} instance.
 * */
public final class CosmosSessionRetryOptionsBuilder {

    private CosmosRegionSwitchHint regionSwitchHint;

    /**
     * Sets the {@link CosmosRegionSwitchHint} which specifies for
     * a request whether internal retry policies should prioritize a local region or a remote region.
     * */
    public CosmosSessionRetryOptionsBuilder setRegionSwitchHint(CosmosRegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint = regionSwitchHint;
        return this;
    }

    /**
     * Builds an instance of {@link CosmosSessionRetryOptions}.
     * */
    public CosmosSessionRetryOptions build() {
        return new CosmosSessionRetryOptions(regionSwitchHint);
    }
}
