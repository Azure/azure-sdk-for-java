// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.config;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.GlobalThroughputControlConfig;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ThroughputControlGroupFactory {

    public static LocalThroughputControlGroup createThroughputLocalControlGroup(ThroughputControlGroupConfig groupConfig, CosmosAsyncContainer targetContainer) {
        checkNotNull(groupConfig, "Throughput control group config can not be null");
        checkNotNull(targetContainer, "Throughput target container can not be null");

        return new LocalThroughputControlGroup(
            groupConfig.getGroupName(),
            targetContainer,
            groupConfig.getTargetThroughput(),
            groupConfig.getTargetThroughputThreshold(),
            groupConfig.isDefault());
    }

    public static GlobalThroughputControlGroup createThroughputGlobalControlGroup(
        ThroughputControlGroupConfig groupConfig,
        GlobalThroughputControlConfig globalControlConfig,
        CosmosAsyncContainer targetContainer) {

        checkNotNull(groupConfig, "Throughput control group config can not be null");
        checkNotNull(globalControlConfig, "Throughput global control config can not be null");
        checkNotNull(targetContainer, "Throughput target container can not be null");

        return new GlobalThroughputControlGroup(
                groupConfig.getGroupName(),
                targetContainer,
                groupConfig.getTargetThroughput(),
                groupConfig.getTargetThroughputThreshold(),
                groupConfig.isDefault(),
                BridgeInternal.getControlContainerFromThroughputGlobalControlConfig(globalControlConfig),
                globalControlConfig.getControlItemRenewInterval(),
                globalControlConfig.getControlItemExpireInterval());

    }
}
