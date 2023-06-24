// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * {@link SessionRetryOptions} encapsulates hints which influence
 * internal retry policies which are applied when the effective consistency
 * used for the request is <i>Session Consistency</i>.
 * */
public final class SessionRetryOptions {

    private CosmosRegionSwitchHint regionSwitchHint;

    /**
     * Instantiates {@link SessionRetryOptions}
     * */
    public SessionRetryOptions() {
        this.regionSwitchHint = CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED;
    }

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
     * @return This instance of {@link SessionRetryOptions}
     * */
    public SessionRetryOptions setRegionSwitchHint(CosmosRegionSwitchHint regionSwitchHint) {
        checkNotNull(regionSwitchHint, "regionSwitchHint cannot be null.");
        this.regionSwitchHint = regionSwitchHint;
        return this;
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
