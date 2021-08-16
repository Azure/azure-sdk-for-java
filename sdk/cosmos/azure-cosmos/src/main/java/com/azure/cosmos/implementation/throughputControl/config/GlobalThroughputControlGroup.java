// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.config;

import com.azure.cosmos.CosmosAsyncContainer;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalThroughputControlGroup extends ThroughputControlGroupInternal {
    private static final Duration DEFAULT_CONTROL_ITEM_RENEW_INTERVAL = Duration.ofSeconds(5);

    private final CosmosAsyncContainer globalControlContainer;
    private final Duration controlItemRenewInterval;
    private final Duration controlItemExpireInterval;

    public GlobalThroughputControlGroup(
        String groupName,
        CosmosAsyncContainer targetContainer,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        boolean isDefault,
        CosmosAsyncContainer globalControlContainer,
        Duration controlItemRenewInterval,
        Duration controlItemExpireInterval) {

        super (groupName, targetContainer, targetThroughput, targetThroughputThreshold, isDefault);

        checkNotNull(globalControlContainer, "Global control container can not be null");

        this.globalControlContainer = globalControlContainer;
        this.controlItemRenewInterval = getDefaultControlItemRenewInterval(controlItemRenewInterval, controlItemRenewInterval);
        this.controlItemExpireInterval =
            controlItemExpireInterval != null ? controlItemExpireInterval : Duration.ofSeconds(2 * this.controlItemRenewInterval.getSeconds() + 1);
    }

    private Duration getDefaultControlItemRenewInterval(Duration controlItemRenewInterval, Duration controlItemExpireInterval) {
        if (controlItemRenewInterval != null) {
            return controlItemRenewInterval;
        }

        if (controlItemExpireInterval != null) {
            return Duration.ofSeconds((controlItemExpireInterval.getSeconds() - 1) / 2);
        }

        return DEFAULT_CONTROL_ITEM_RENEW_INTERVAL;
    }

    /**
     * Get the control container.
     * This is the container to track all other clients throughput usage.
     *
     * @return The {@link CosmosAsyncContainer}.
     */
    public CosmosAsyncContainer getGlobalControlContainer() {
        return globalControlContainer;
    }

    /**
     * Get the control item renew interval.
     *
     * This controls how often the client is going to update the throughput usage of itself
     * and adjust its own throughput share based on the throughput usage of other clients.
     *
     * In short words, it controls how quickly the shared throughput will reload balanced across different clients.
     *
     * @return The control item renew interval.
     */
    public Duration getControlItemRenewInterval() {
        return controlItemRenewInterval;
    }


    /**
     * Get the control item expire interval.
     *
     * A client may be offline due to various reasons (being shutdown, network issue... ).
     * This controls how quickly we will detect the client has been offline and hence allow its throughput share to be taken by other clients.
     **
     * @return The control item renew interval.
     */
    public Duration getControlItemExpireInterval() {
        return controlItemExpireInterval;
    }
}
