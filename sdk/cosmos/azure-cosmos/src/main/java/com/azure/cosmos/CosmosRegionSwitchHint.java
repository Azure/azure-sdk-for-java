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
     * {@link  CosmosRegionSwitchHint#CURRENT_REGION_PREFERRED} corresponds to a hint
     * which will result in internal retry policies biasing the current region more
     * than different regions.
     * */
    public static final CosmosRegionSwitchHint CURRENT_REGION_PREFERRED =
            new CosmosRegionSwitchHint("currentRegionPreferred");

    /**
     * {@link CosmosRegionSwitchHint#DIFFERENT_REGION_PREFERRED} corresponds to a hint
     * which will result in internal retry policies biasing different regions more
     * than the current region.
     * */
    public static final CosmosRegionSwitchHint DIFFERENT_REGION_PREFERRED =
            new CosmosRegionSwitchHint("differentRegionPreferred");

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
