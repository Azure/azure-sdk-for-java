// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.sdk.controller.group;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.sdk.LinkedCancellationToken;
import com.azure.cosmos.implementation.throughputControl.sdk.config.SDKThroughputControlGroupInternal;
import com.azure.cosmos.implementation.throughputControl.sdk.config.GlobalThroughputControlGroup;
import com.azure.cosmos.implementation.throughputControl.sdk.config.LocalThroughputControlGroup;
import com.azure.cosmos.implementation.throughputControl.sdk.controller.group.global.GlobalThroughputControlGroupController;
import com.azure.cosmos.implementation.throughputControl.sdk.controller.group.local.LocalThroughputControlGroupController;

public class ThroughputGroupControllerFactory {

    public static SDKThroughputGroupControllerBase createController(
        ConnectionMode connectionMode,
        SDKThroughputControlGroupInternal group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetCollectionRid,
        LinkedCancellationToken parentToken) {

        if (group instanceof LocalThroughputControlGroup) {
            return new LocalThroughputControlGroupController(
                connectionMode,
                (LocalThroughputControlGroup) group,
                maxContainerThroughput,
                partitionKeyRangeCache,
                targetCollectionRid,
                parentToken);
        } else if (group instanceof GlobalThroughputControlGroup) {
            return new GlobalThroughputControlGroupController(
                connectionMode,
                (GlobalThroughputControlGroup) group,
                maxContainerThroughput,
                partitionKeyRangeCache,
                targetCollectionRid,
                parentToken);
        }

        throw new IllegalArgumentException(String.format("Throughput group control group %s is not supported", group.getClass()));
    }
}
