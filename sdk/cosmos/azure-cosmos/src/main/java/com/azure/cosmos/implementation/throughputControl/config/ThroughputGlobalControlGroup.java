package com.azure.cosmos.implementation.throughputControl.config;

import com.azure.cosmos.CosmosAsyncContainer;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ThroughputGlobalControlGroup extends ThroughputControlGroupInternal {
    private static final Duration DEFAULT_CONTROL_ITEM_RENEW_INTERVAL = Duration.ofSeconds(10);

    private final CosmosAsyncContainer globalControlContainer;
    private final Duration controlItemRenewInterval;
    private final Duration controlItemExpireInterval;

    public ThroughputGlobalControlGroup(
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
        this.controlItemRenewInterval = controlItemRenewInterval != null ? controlItemRenewInterval : DEFAULT_CONTROL_ITEM_RENEW_INTERVAL;
        this.controlItemExpireInterval =
            controlItemExpireInterval != null ? controlItemExpireInterval : Duration.ofSeconds(2 * this.controlItemRenewInterval.toSeconds());
    }

    public CosmosAsyncContainer getGlobalControlContainer() {
        return globalControlContainer;
    }

    public Duration getControlItemRenewInterval() {
        return controlItemRenewInterval;
    }

    public Duration getControlItemExpireInterval() {
        return controlItemExpireInterval;
    }
}
