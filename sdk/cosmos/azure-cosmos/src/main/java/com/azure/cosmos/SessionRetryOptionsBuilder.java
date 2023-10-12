// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * A {@link SessionRetryOptionsBuilder} instance will be used to build
 * a {@link SessionRetryOptions} instance.
 * */
public final class SessionRetryOptionsBuilder {

    private CosmosRegionSwitchHint regionSwitchHint;
    private Duration minInRegionRetryTimeForWriteOperations = Configs.DEFAULT_MIN_IN_REGION_RETRY_TIME_FOR_WRITES;

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
     * Sets the minimum retry time for 404/1002 retries within each region for write operations. The minimum value
     * is 100ms - this minimum is enforced to provide a way for the local region to catch-up on replication lag.
     * @param minRetryTime the min retry time to be used with-in each region for write operations
     * @return This instance of {@link SessionRetryOptionsBuilder}
     */
    public SessionRetryOptionsBuilder minRetryTimeInLocalRegionForWriteOperations(Duration minRetryTime) {
        this.minInRegionRetryTimeForWriteOperations = minRetryTime;
        return this;
    }

    /**
     * Builds an instance of {@link SessionRetryOptions}
     *
     * @return An instance of {@link SessionRetryOptions}
     * */
    public SessionRetryOptions build() {
        checkNotNull(regionSwitchHint, "regionSwitch hint cannot be null");

        if (regionSwitchHint == CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED) {
            checkArgument(
                minInRegionRetryTimeForWriteOperations != null,
                "Argument 'minInRegionRetryTimeForWriteOperations' must not be null when 'regionSwitchHint' "
                    + "is 'REMOTE_REGION_PREFERRED'.");

            checkArgument(
                minInRegionRetryTimeForWriteOperations
                    .compareTo(Configs.MIN_MIN_IN_REGION_RETRY_TIME_FOR_WRITES) >= 0,
                "Argument 'minInRegionRetryTimeForWriteOperations' must have at least a value of '"
                    + Configs.MIN_MIN_IN_REGION_RETRY_TIME_FOR_WRITES.toString()
                    + "' when 'regionSwitchHint' is 'REMOTE_REGION_PREFERRED'.");
        }

        return new SessionRetryOptions(regionSwitchHint, minInRegionRetryTimeForWriteOperations);
    }
}