// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget.controller.group;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ThroughputBudgetGroupConfig;
import com.azure.cosmos.ThroughputBudgetGroupControlMode;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputBudget.ThroughputBudgetGroupConfigInternal;

public class ThroughputBudgetGroupControllerFactory {

    public static ThroughputBudgetGroupControllerBase createController(
        ConnectionMode connectionMode,
        ThroughputBudgetGroupConfig groupConfig,
        String hostName,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetCollectionRid) {

        ThroughputBudgetGroupConfigInternal groupConfigInternal = new ThroughputBudgetGroupConfigInternal(groupConfig,hostName, targetCollectionRid);

        if (groupConfig.getControlMode() == ThroughputBudgetGroupControlMode.LOCAL) {
            return new ThroughputBudgetGroupLocalController(
                connectionMode,
                groupConfigInternal,
                maxContainerThroughput,
                partitionKeyRangeCache);
        } else if (groupConfig.getControlMode() == ThroughputBudgetGroupControlMode.DISTRIBUTED) {
            return new ThroughputBudgetGroupDistributedController(
                connectionMode,
                groupConfigInternal,
                maxContainerThroughput,
                partitionKeyRangeCache);
        }

        throw new IllegalArgumentException(String.format("Throughput budget group control mode %s is not supported", groupConfig.getControlMode()));
    }
}
