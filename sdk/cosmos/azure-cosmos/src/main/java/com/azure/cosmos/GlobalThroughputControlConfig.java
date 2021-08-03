// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.util.Beta;

import java.time.Duration;

/**
 * This configuration is used for throughput global control mode.
 * It contains configuration about the extra container which will track all the clients throughput usage for a certain control group.
 */
@Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class GlobalThroughputControlConfig {
    private final CosmosAsyncContainer controlContainer;
    private final Duration controlItemRenewInterval;
    private final Duration controlItemExpireInterval;

    GlobalThroughputControlConfig(
        CosmosAsyncContainer controlContainer,
        Duration controlItemRenewInterval,
        Duration controlItemExpireInterval) {

        this.controlContainer = controlContainer;
        this.controlItemRenewInterval = controlItemRenewInterval;
        this.controlItemExpireInterval = controlItemExpireInterval;
    }

    /**
     * Get the control container.
     * This is the container to track all other clients throughput usage.
     *
     * @return The {@link CosmosAsyncContainer}.
     */
    CosmosAsyncContainer getControlContainer() {
        return controlContainer;
    }

    /**
     * Get the control item renew interval.
     *
     * This controls how often the client is going to update the throughput usage of itself
     * and adjust its own throughput share based on the throughput usage of other clients.
     *
     * In short words, it controls how quickly the shared throughput will reload balanced across different clients.
     *
     * The allowed min value is 5s. By default, it is 5s.
     *
     * @return The control item renew interval.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getControlItemRenewInterval() {
        return this.controlItemRenewInterval;
    }

    /**
     * Get the control item expire interval.
     *
     * A client may be offline due to various reasons (being shutdown, network issue... ).
     * This controls how quickly we will detect the client has been offline and hence allow its throughput share to be taken by other clients.
     *
     * The allowed min value is 2 * controlItemRenewInterval + 1. By default, it is 11s.
     *
     * @return The control item renew interval.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getControlItemExpireInterval() {
        return this.controlItemExpireInterval;
    }
}
