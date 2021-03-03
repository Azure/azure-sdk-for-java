// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.local;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.LinkedCancellationToken;
import com.azure.cosmos.implementation.throughputControl.config.LocalThroughputControlGroup;
import com.azure.cosmos.implementation.throughputControl.controller.group.ThroughputGroupControllerBase;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class LocalThroughputControlGroupController extends ThroughputGroupControllerBase {

    public LocalThroughputControlGroupController(
        ConnectionMode connectionMode,
        LocalThroughputControlGroup group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid,
        LinkedCancellationToken parentToken) {

        super(connectionMode, group, maxContainerThroughput, partitionKeyRangeCache, targetContainerRid, parentToken);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.resolveRequestController()
            .doOnSuccess(dummy -> {
                this.throughputUsageCycleRenewTask(this.cancellationTokenSource.getToken()).publishOn(Schedulers.parallel()).subscribe();
            })
            .thenReturn((T)this);
    }

    @Override
    public double getClientAllocatedThroughput() {
        return this.groupThroughput.get();
    }

    @Override
    public void recordThroughputUsage(double loadFactor) {
        return;
    }
}
