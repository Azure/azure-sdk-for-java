// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlMode;

public class ThroughputGroupControllerFactory {

    public static ThroughputGroupControllerBase createController(
        ConnectionMode connectionMode,
        GlobalEndpointManager globalEndpointManager,
        ThroughputControlGroup group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetCollectionRid) {

        ThroughputControlMode controlMode = BridgeInternal.getThroughputControlMode(group);
        if (controlMode == ThroughputControlMode.LOCAL) {
            return new ThroughputGroupLocalController(
                connectionMode,
                globalEndpointManager,
                group,
                maxContainerThroughput,
                partitionKeyRangeCache,
                targetCollectionRid);
        }

        // TODO: distributed mode support

        throw new IllegalArgumentException(String.format("Throughput group control mode %s is not supported", controlMode));
    }
}
