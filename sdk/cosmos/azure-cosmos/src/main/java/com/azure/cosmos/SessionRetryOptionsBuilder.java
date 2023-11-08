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
    private Duration minInRegionRetryTime = Configs.getMinRetryTimeInLocalRegionWhenRemoteRegionPreferred();

    private int maxInRegionRetryCount = Configs.getMaxRetriesInLocalRegionWhenRemoteRegionPreferred();

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
     * Sets the minimum retry time for 404/1002 retries within each region for read and write operations. The minimum
     *  value is 100ms - this minimum is enforced to provide a way for the local region to catch-up on replication lag.
     *  The default value is 500ms - as a recommendation ensure that this value is higher than the steady-state
     *  replication latency between the regions you chose.
     * @param minTimeoutPerRegion the min retry time to be used with-in each region
     * @return This instance of {@link SessionRetryOptionsBuilder}
     */
    public SessionRetryOptionsBuilder minTimeoutPerRegion(Duration minTimeoutPerRegion) {
        this.minInRegionRetryTime = minTimeoutPerRegion;
        return this;
    }

    /**
     * Sets the maximum number of retries within each region for read and write operations. The minimum
     *  value is 1 - the backoff time for the last in-region retry will ensure that the total retry time within the
     *  region is at least the min. in-region retry time.
     * @param maxRetriesPerRegion the max. number of retries with-in each region
     * @return This instance of {@link SessionRetryOptionsBuilder}
     */
    public SessionRetryOptionsBuilder maxRetriesPerRegion(int maxRetriesPerRegion) {
        this.maxInRegionRetryCount = maxRetriesPerRegion;
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
                minInRegionRetryTime != null,
                "Argument 'minInRegionRetryTimeForWriteOperations' must not be null when 'regionSwitchHint' "
                    + "is 'REMOTE_REGION_PREFERRED'.");

            checkArgument(
                minInRegionRetryTime
                    .compareTo(Duration.ofMillis(Configs.MIN_MIN_IN_REGION_RETRY_TIME_FOR_WRITES_MS)) >= 0,
                "Argument 'minInRegionRetryTime' must have at least a value of '"
                    + Duration.ofMillis(Configs.MIN_MIN_IN_REGION_RETRY_TIME_FOR_WRITES_MS)
                    + "' when 'regionSwitchHint' is 'REMOTE_REGION_PREFERRED'.");

            checkArgument(
                maxInRegionRetryCount >= Configs.MIN_MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED,
                "Argument 'maxInRegionRetryCount' must have at least a value of '"
                    + Configs.MIN_MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED
                    + "' when 'regionSwitchHint' is 'REMOTE_REGION_PREFERRED'.");
        }

        return new SessionRetryOptions(regionSwitchHint, minInRegionRetryTime, maxInRegionRetryCount);
    }
}