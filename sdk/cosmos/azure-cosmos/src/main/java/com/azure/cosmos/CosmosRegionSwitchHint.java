// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * {@link CosmosRegionSwitchHint} encapsulates hints which guide SDK-internal
 * retry policies on how early to switch retries to a different region.
 */
public final class CosmosRegionSwitchHint {

    private CosmosRegionSwitchHint() {}

    /**
     * {@link  CosmosRegionSwitchHint#LOCAL_REGION_PREFERRED} corresponds to a hint
     * which will result in internal retry policies biasing the local region of a request
     * more than remote regions.
     * */
    public static final CosmosRegionSwitchHint LOCAL_REGION_PREFERRED = new CosmosRegionSwitchHint();

    /**
     * {@link CosmosRegionSwitchHint#REMOTE_REGION_PREFERRED} corresponds to a hint
     * which will result in internal retry policies biasing remote regions more
     * than the local region.
     * */
    public static final CosmosRegionSwitchHint REMOTE_REGION_PREFERRED = new CosmosRegionSwitchHint();
}
