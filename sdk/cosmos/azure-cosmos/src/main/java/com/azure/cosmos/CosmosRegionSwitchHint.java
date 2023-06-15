// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.util.Objects;

/**
 * {@link CosmosRegionSwitchHint} encapsulates hints which guide SDK-internal
 * retry policies on how early to switch retries to a different region.
 */
public final class CosmosRegionSwitchHint {

    private final String hintRepresentation;

    /**
     * {@link  CosmosRegionSwitchHint#LOCAL_REGION_PREFERRED} corresponds to a hint
     * which will result in internal retry policies biasing the local region of a request
     * more than remote regions.
     * */
    public static final CosmosRegionSwitchHint LOCAL_REGION_PREFERRED =
            new CosmosRegionSwitchHint("localRegionPreferred");

    /**
     * {@link CosmosRegionSwitchHint#REMOTE_REGION_PREFERRED} corresponds to a hint
     * which will result in internal retry policies biasing remote regions more
     * than the local region.
     * */
    public static final CosmosRegionSwitchHint REMOTE_REGION_PREFERRED =
            new CosmosRegionSwitchHint("remoteRegionPreferred");

    private CosmosRegionSwitchHint(String hintRepresentation) {
        this.hintRepresentation = hintRepresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CosmosRegionSwitchHint that = (CosmosRegionSwitchHint) o;
        return Objects.equals(hintRepresentation, that.hintRepresentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hintRepresentation);
    }
}
