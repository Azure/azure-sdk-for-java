// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.LinkedCancellationToken;
import com.azure.cosmos.implementation.throughputControl.config.GlobalThroughputControlSimpleGroup;
import com.azure.cosmos.implementation.throughputControl.controller.group.ThroughputGroupControllerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * Equally distribute the throughput between all the instances.
 * Attention: Only used internally by spark.
 */
public class GlobalThroughputControlSimpleGroupController extends ThroughputGroupControllerBase {
    private static final Logger logger = LoggerFactory.getLogger(GlobalThroughputControlSimpleGroupController.class);
    private final Callable<Integer> instanceCountCallable;
    private final AtomicInteger instanceCount;

    public GlobalThroughputControlSimpleGroupController(
        ConnectionMode connectionMode,
        GlobalThroughputControlSimpleGroup group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid,
        LinkedCancellationToken parentToken) {

        super(connectionMode, group, maxContainerThroughput, partitionKeyRangeCache, targetContainerRid, parentToken);
        this.instanceCountCallable = group.getInstanceCountCallable();
        this.instanceCount = new AtomicInteger(1); // start with 1
        logger.info("Simple group is initialized");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.resolveRequestController()
            .doOnSuccess(dummy -> {
                this.throughputUsageCycleRenewTask(this.cancellationTokenSource.getToken())
                    .publishOn(CosmosSchedulers.COSMOS_PARALLEL)
                    .subscribe();
            })
            .thenReturn((T)this);
    }

    @Override
    public double getClientAllocatedThroughput() {
        try {
            int newInstanceCount = this.instanceCountCallable.call();
            this.instanceCount.set(newInstanceCount);
        } catch (Exception e) {
            logger.warn("Getting instance count failed", e);
        }

        return this.groupThroughput.get() / this.instanceCount.get();
    }

    @Override
    public void recordThroughputUsage(double loadFactor) {
        // no-op
    }
}
