// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosRegionSwitchHint;

/**
 *
 * */
public final class CosmosSessionRetryOptions {

    private final CosmosRegionSwitchHint regionSwitchHint;

    CosmosSessionRetryOptions(CosmosRegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint = regionSwitchHint;
    }

    // TODO: Do not expose
    public CosmosRegionSwitchHint getRegionSwitchHint() {
        return regionSwitchHint;
    }
}
