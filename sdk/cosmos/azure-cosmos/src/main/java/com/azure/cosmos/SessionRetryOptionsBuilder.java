// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * A {@link SessionRetryOptionsBuilder} instance will be used to build
 * a {@link SessionRetryOptions} instance.
 * */
public final class SessionRetryOptionsBuilder {

    private CosmosRegionSwitchHint regionSwitchHint;

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
     * @return This instance of {@link SessionRetryOptionsBuilder}
     * */
    public SessionRetryOptionsBuilder regionSwitchHint(CosmosRegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint = regionSwitchHint;
        return this;
    }

    /**
     * Builds an instance of {@link SessionRetryOptions}
     *
     * @return An instance of {@link SessionRetryOptions}
     * */
    public SessionRetryOptions build() {
        checkNotNull(regionSwitchHint, "regionSwitch hint cannot be null");
        return new SessionRetryOptions(regionSwitchHint);
    }
}