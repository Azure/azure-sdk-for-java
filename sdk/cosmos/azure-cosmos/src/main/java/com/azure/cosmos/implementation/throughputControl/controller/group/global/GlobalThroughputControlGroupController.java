// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.guava25.collect.EvictingQueue;
import com.azure.cosmos.implementation.throughputControl.LinkedCancellationToken;
import com.azure.cosmos.implementation.throughputControl.config.GlobalThroughputControlGroup;
import com.azure.cosmos.implementation.throughputControl.controller.group.ThroughputGroupControllerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalThroughputControlGroupController extends ThroughputGroupControllerBase {
    private static final Logger logger = LoggerFactory.getLogger(GlobalThroughputControlGroupController.class);
    private static final double INITIAL_CLIENT_THROUGHPUT_RU_SHARE = 1.0;
    private static final double INITIAL_THROUGHPUT_USAGE = 1.0;
    private static final int DEFAULT_THROUGHPUT_USAGE_QUEUE_SIZE = 300; // 5 mins windows since we refresh ru usage every 1s
    private static final double MIN_LOAD_FACTOR = 0.1;

    private final Duration controlItemRenewInterval;
    private final ThroughputControlContainerManager containerManager;
    private final EvictingQueue<ThroughputUsageSnapshot> throughputUsageSnapshotQueue;
    private final Object throughputUsageSnapshotQueueLock;
    private AtomicReference<Double> clientThroughputShare;

    public GlobalThroughputControlGroupController(
        ConnectionMode connectionMode,
        GlobalThroughputControlGroup group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid,
        LinkedCancellationToken parentToken) {
        super(connectionMode, group, maxContainerThroughput, partitionKeyRangeCache, targetContainerRid, parentToken);

        this.controlItemRenewInterval = group.getControlItemRenewInterval();
        this.containerManager = new ThroughputControlContainerManager(group);

        this.throughputUsageSnapshotQueue = EvictingQueue.create(DEFAULT_THROUGHPUT_USAGE_QUEUE_SIZE);
        this.throughputUsageSnapshotQueue.add(new ThroughputUsageSnapshot(INITIAL_THROUGHPUT_USAGE));
        this.throughputUsageSnapshotQueueLock = new Object();
        this.clientThroughputShare = new AtomicReference<>(INITIAL_CLIENT_THROUGHPUT_RU_SHARE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.containerManager.validateControlContainer()
            .flatMap(dummy -> this.containerManager.getOrCreateConfigItem())
            .flatMap(dummy -> {
                double loadFactor = this.calculateLoadFactor();
                return this.calculateClientThroughputShare(loadFactor)
                    .flatMap(controller -> this.containerManager.createGroupClientItem(loadFactor, this.getClientAllocatedThroughput()));
            })
            .flatMap(dummy -> this.resolveRequestController())
            .doOnSuccess(dummy -> {
                this.throughputUsageCycleRenewTask(this.cancellationTokenSource.getToken())
                    .publishOn(CosmosSchedulers.COSMOS_PARALLEL)
                    .subscribe();
                this.calculateClientThroughputShareTask(this.cancellationTokenSource.getToken())
                    .publishOn(CosmosSchedulers.COSMOS_PARALLEL)
                    .subscribe();
            })
            .thenReturn((T)this);
    }

    @Override
    public double getClientAllocatedThroughput() {
        return this.groupThroughput.get() * this.clientThroughputShare.get();
    }

    @Override
    public void recordThroughputUsage(double throughputUsage) {
        synchronized (this.throughputUsageSnapshotQueueLock) {
            this.throughputUsageSnapshotQueue.add(new ThroughputUsageSnapshot(throughputUsage));
        }
    }

    private Mono<GlobalThroughputControlGroupController> calculateClientThroughputShare(double loadFactor) {
        return this.containerManager.queryLoadFactorsOfAllClients(loadFactor)
            .doOnSuccess(totalLoads -> this.clientThroughputShare.set(loadFactor / totalLoads))
            .thenReturn(this);
    }

    private double calculateLoadFactor() {
        synchronized (this.throughputUsageSnapshotQueueLock) {
            Instant startTime = this.throughputUsageSnapshotQueue.peek().getTime();

            double totalWeight = 0.0;
            for (ThroughputUsageSnapshot throughputUsageSnapshot : this.throughputUsageSnapshotQueue) {
                totalWeight += throughputUsageSnapshot.calculateWeight(startTime);
            }

            double loadFactor = 0.0;
            for (ThroughputUsageSnapshot throughputUsageSnapshot : this.throughputUsageSnapshotQueue) {
                loadFactor += (throughputUsageSnapshot.getWeight() / totalWeight) * throughputUsageSnapshot.getThroughputUsage();
            }

            return Math.max(MIN_LOAD_FACTOR, loadFactor);
        }
    }

    private Flux<Void> calculateClientThroughputShareTask(LinkedCancellationToken cancellationToken) {
        return Mono.delay(controlItemRenewInterval, CosmosSchedulers.COSMOS_PARALLEL)
            .flatMap(t -> {
                if (cancellationToken.isCancellationRequested()) {
                    return Mono.empty();
                } else {
                    double loadFactor = this.calculateLoadFactor();
                    return this.calculateClientThroughputShare(loadFactor)
                        .flatMap(dummy -> this.containerManager.replaceOrCreateGroupClientItem(loadFactor, this.getClientAllocatedThroughput()));
                }
            })
            .onErrorResume(throwable -> {
                logger.warn("Calculate throughput task failed ", throwable);
                return Mono.empty();
            })
            .then()
            .repeat(() -> !cancellationToken.isCancellationRequested());
    }
}
