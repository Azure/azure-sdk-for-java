// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;

public class ThroughputGroupLocalController extends ThroughputGroupControllerBase {

    public ThroughputGroupLocalController(
        ConnectionMode connectionMode,
        ThroughputControlGroup group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid) {

        super(connectionMode, group, maxContainerThroughput, partitionKeyRangeCache, targetContainerRid);
    }
}
