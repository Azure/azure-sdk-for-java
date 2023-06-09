// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosRegionSwitchHint;

/**
 * {@link CosmosSessionRetryOptions} encapsulates hints which influence
 * internal retry policies which are applied when the effective consistency
 * used for the request is Session Consistency.
 * */
public final class CosmosSessionRetryOptions {

    private final CosmosRegionSwitchHint regionSwitchHint;

    CosmosSessionRetryOptions(CosmosRegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint = regionSwitchHint;
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.setSessionRetryOptionsAccessor(new ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.CosmosSessionRetryOptionsAccessor() {
            @Override
            public CosmosRegionSwitchHint getRegionSwitchHint(CosmosSessionRetryOptions sessionRetryOptions) {
                return sessionRetryOptions.regionSwitchHint;
            }
        });
    }
}
