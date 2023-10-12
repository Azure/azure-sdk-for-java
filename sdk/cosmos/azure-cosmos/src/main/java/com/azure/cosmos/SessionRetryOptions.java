// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

import java.time.Duration;

/**
 * {@link SessionRetryOptions} encapsulates hints which influence
 * internal retry policies which are applied when the effective consistency
 * used for the request is <i>Session Consistency</i>.
 * */
public final class SessionRetryOptions {

    private final CosmosRegionSwitchHint regionSwitchHint;
    private final Duration minInRegionRetryTimeForWriteOperations;

    /**
     * Instantiates {@link SessionRetryOptions}
     * */
    SessionRetryOptions(CosmosRegionSwitchHint regionSwitchHint, Duration minInRegionRetryTimeForWriteOperations) {
        this.regionSwitchHint = regionSwitchHint;
        this.minInRegionRetryTimeForWriteOperations = minInRegionRetryTimeForWriteOperations;
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.setCosmosSessionRetryOptionsAccessor(

            new ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.CosmosSessionRetryOptionsAccessor() {
                @Override
                public CosmosRegionSwitchHint getRegionSwitchHint(SessionRetryOptions sessionRetryOptions) {
                    return sessionRetryOptions.regionSwitchHint;
                }

                @Override
                public Duration getMinInRegionRetryTimeForWriteOperations(SessionRetryOptions sessionRetryOptions) {
                    return sessionRetryOptions.minInRegionRetryTimeForWriteOperations;
                }
            });
    }

    static { initialize(); }
}
