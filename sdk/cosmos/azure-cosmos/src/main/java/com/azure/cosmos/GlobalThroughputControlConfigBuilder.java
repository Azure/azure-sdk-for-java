// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Throughput global control config builder.
 */
public class GlobalThroughputControlConfigBuilder {
    private final CosmosAsyncContainer controlContainer;

    private Duration controlItemRenewInterval;
    private Duration controlItemExpireInterval;

    GlobalThroughputControlConfigBuilder(CosmosAsyncClient client, String databaseId, String containerId) {
        checkNotNull(client, "Client can not be null");
        checkArgument(StringUtils.isNotEmpty(databaseId), "DatabaseId cannot be null nor empty");
        checkArgument(StringUtils.isNotEmpty(containerId), "ContainerId cannot be null nor empty");

        controlContainer = client.getDatabase(databaseId).getContainer(containerId);
    }

    /**
     * Set the control item renew interval.
     *
     * This controls how often the client is going to update the throughput usage of itself
     * and adjust its own throughput share based on the throughput usage of other clients.
     *
     * In short words, it controls how quickly the shared throughput will reload balanced across different clients.
     *
     *
     * @param controlItemRenewInterval The control item renewal interval.
     * @return The {@link GlobalThroughputControlConfigBuilder}
     */
    public GlobalThroughputControlConfigBuilder setControlItemRenewInterval(Duration controlItemRenewInterval) {
        checkArgument(controlItemRenewInterval.getSeconds() >= 5, "Renew interval should be no less than 5s");
        this.controlItemRenewInterval = controlItemRenewInterval;
        return this;
    }

    /**
     * Set the control item expire interval.
     *
     * A client may be offline due to various reasons (being shutdown, network issue... ).
     * This controls how quickly we will detect the client has been offline and hence allow its throughput share to be taken by other clients.
     *
     * @param controlItemExpireInterval The control item expire interval.
     * @return The {@link GlobalThroughputControlConfigBuilder}
     */
    public GlobalThroughputControlConfigBuilder setControlItemExpireInterval(Duration controlItemExpireInterval) {
        this.controlItemExpireInterval = controlItemExpireInterval;
        return this;
    }

    /**
     * Validate the throughput global control configuration and create a new throughput global control config item.
     *
     * @return A new {@link GlobalThroughputControlConfig}.
     */
    public GlobalThroughputControlConfig build() {
        if (this.controlItemExpireInterval != null && this.controlItemRenewInterval != null) {
            // expireInterval will be used as the ttl of ThroughputGlobalControlClientItem, it should be a reasonable value
            // to make sure the item is not get deleted too fast
            if (this.controlItemExpireInterval.getSeconds() < 2 * this.controlItemRenewInterval.getSeconds() + 1) {
                throw new IllegalArgumentException("ControlItemExpireInterval is too small compared to ControlItemExpireInterval");
            }
        }

        return new GlobalThroughputControlConfig(this.controlContainer, this.controlItemRenewInterval, this.controlItemExpireInterval);
    }
}
