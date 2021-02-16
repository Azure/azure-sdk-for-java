// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.util.Beta;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Throughput global control config builder.
 */
@Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class ThroughputGlobalControlConfigBuilder {
    private final CosmosAsyncContainer controlContainer;

    private Duration controlItemRenewInterval;
    private Duration controlItemExpireInterval;

    ThroughputGlobalControlConfigBuilder(CosmosAsyncClient client, String databaseId, String containerId) {
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
     * @return The {@link ThroughputGlobalControlConfigBuilder}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputGlobalControlConfigBuilder setControlItemRenewInterval(Duration controlItemRenewInterval) {
        this.controlItemRenewInterval = controlItemRenewInterval;
        return this;
    }

    /**
     * Set the control item expire interval.
     *
     * A client may be offline due to various reasons (being shutdown, network issue... ).
     * This controls how quickly we will detect the client has been offline and hence allow its throughput share to be taken by other clients.
     **
     * @return The {@link ThroughputGlobalControlConfigBuilder}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputGlobalControlConfigBuilder setControlItemExpireInterval(Duration controlItemExpireInterval) {
        this.controlItemExpireInterval = controlItemExpireInterval;
        return this;
    }

    /**
     * Validate the throughput global control configuration and create a new throughput global control config item.
     *
     * @return A new {@link ThroughputGlobalControlConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputGlobalControlConfig build() {
        if (this.controlItemExpireInterval.getSeconds() < this.controlItemRenewInterval.getSeconds()) {
            throw new IllegalArgumentException("Expire time should not be smaller than renew interval");
        }

        return new ThroughputGlobalControlConfig(this.controlContainer, this.controlItemRenewInterval, this.controlItemExpireInterval);
    }
}
