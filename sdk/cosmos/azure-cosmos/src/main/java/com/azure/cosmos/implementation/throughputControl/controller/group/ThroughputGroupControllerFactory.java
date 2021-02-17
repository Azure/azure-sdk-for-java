// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.LinkedCancellationToken;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputGlobalControlGroup;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputLocalControlGroup;
import com.azure.cosmos.implementation.throughputControl.controller.group.global.ThroughputGroupGlobalController;
import com.azure.cosmos.implementation.throughputControl.controller.group.local.ThroughputGroupLocalController;

public class ThroughputGroupControllerFactory {

    public static ThroughputGroupControllerBase createController(
        ConnectionMode connectionMode,
        GlobalEndpointManager globalEndpointManager,
        ThroughputControlGroupInternal group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetCollectionRid,
        LinkedCancellationToken parentToken) {

        if (group instanceof ThroughputLocalControlGroup) {
            return new ThroughputGroupLocalController(
                connectionMode,
                globalEndpointManager,
                (ThroughputLocalControlGroup) group,
                maxContainerThroughput,
                partitionKeyRangeCache,
                targetCollectionRid,
                parentToken);
        } else if (group instanceof ThroughputGlobalControlGroup) {
            return new ThroughputGroupGlobalController(
                connectionMode,
                globalEndpointManager,
                (ThroughputGlobalControlGroup) group,
                maxContainerThroughput,
                partitionKeyRangeCache,
                targetCollectionRid,
                parentToken);
        }

        throw new IllegalArgumentException(String.format("Throughput group control group %s is not supported", group.getClass()));
    }
}
