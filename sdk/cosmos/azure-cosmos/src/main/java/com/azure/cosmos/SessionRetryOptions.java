// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

/**
 * {@link SessionRetryOptions} encapsulates hints which influence
 * internal retry policies which are applied when the effective consistency
 * used for the request is <i>Session Consistency</i>.
 * */
public final class SessionRetryOptions {

    private final CosmosRegionSwitchHint regionSwitchHint;

    /**
     * Instantiates {@link SessionRetryOptions}
     * */
    SessionRetryOptions(CosmosRegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint = regionSwitchHint;
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.setCosmosSessionRetryOptionsAccessor(

            new ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.CosmosSessionRetryOptionsAccessor() {
                @Override
                public CosmosRegionSwitchHint getRegionSwitchHint(SessionRetryOptions sessionRetryOptions) {
                    return sessionRetryOptions.regionSwitchHint;
                }
            });
    }

    static { initialize(); }
}
