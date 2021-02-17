// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.local;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.LinkedCancellationToken;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputLocalControlGroup;
import com.azure.cosmos.implementation.throughputControl.controller.group.ThroughputGroupControllerBase;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ThroughputGroupLocalController extends ThroughputGroupControllerBase {
    private static final double CLIENT_THROUGHPUT_SHARE = 1.0;

    public ThroughputGroupLocalController(
        ConnectionMode connectionMode,
        GlobalEndpointManager globalEndpointManager,
        ThroughputLocalControlGroup group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid,
        LinkedCancellationToken parentToken) {

        super(connectionMode, globalEndpointManager, group, maxContainerThroughput, partitionKeyRangeCache, targetContainerRid, parentToken);
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
    public double getClientThroughputShare() {
        return CLIENT_THROUGHPUT_SHARE;
    }

    @Override
    public void recordThroughputUsage(double loadFactor) {
        return;
    }
}
