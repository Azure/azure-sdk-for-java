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
    private final Duration minInRegionRetryTime;
    private final int maxInRegionRetryCount;
    private final String sessionRetryOptionsAsString;

    /**
     * Instantiates {@link SessionRetryOptions}
     * */
    SessionRetryOptions(CosmosRegionSwitchHint regionSwitchHint,
                        Duration minInRegionRetryTime,
                        int maxInRegionRetryCount) {
        this.regionSwitchHint = regionSwitchHint;
        this.minInRegionRetryTime = minInRegionRetryTime ;
        this.maxInRegionRetryCount = maxInRegionRetryCount;
        this.sessionRetryOptionsAsString = sessionRetryOptionsAsString(this.regionSwitchHint, this.minInRegionRetryTime, this.maxInRegionRetryCount);
    }

    @Override
    public String toString() {
        return this.sessionRetryOptionsAsString;
    }

    private static String regionSwitchHintToString(CosmosRegionSwitchHint regionSwitchHint) {

        String regionSwitchHintAsString = "";

        if (regionSwitchHint == null) {
            return regionSwitchHintAsString;
        }

        if (regionSwitchHint == CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED) {
            regionSwitchHintAsString = "REMOTE_REGION_PREFERRED";
        }

        if (regionSwitchHint == CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED) {
            regionSwitchHintAsString = "LOCAL_REGION_PREFERRED";
        }

        return regionSwitchHintAsString;
    }

    private static String sessionRetryOptionsAsString(CosmosRegionSwitchHint regionSwitchHint,
                                                      Duration minInRegionRetryTime,
                                                      int maxInRegionRetryCount) {
        return String.format(
            "(rsh:%s, minrrt:%s, maxrrc:%s)",
            regionSwitchHintToString(regionSwitchHint),
            minInRegionRetryTime.toString(),
            maxInRegionRetryCount
        );
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.setCosmosSessionRetryOptionsAccessor(

            new ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.CosmosSessionRetryOptionsAccessor() {
                @Override
                public CosmosRegionSwitchHint getRegionSwitchHint(SessionRetryOptions sessionRetryOptions) {
                    return sessionRetryOptions.regionSwitchHint;
                }

                @Override
                public Duration getMinInRegionRetryTime(SessionRetryOptions sessionRetryOptions) {
                    return sessionRetryOptions.minInRegionRetryTime;
                }

                @Override
                public int getMaxInRegionRetryCount(SessionRetryOptions sessionRetryOptions) {
                    return sessionRetryOptions.maxInRegionRetryCount;
                }
            });
    }

    static { initialize(); }
}
