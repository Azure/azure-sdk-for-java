// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.throughputControl.controller.request.GlobalThroughputRequestController;
import com.azure.cosmos.implementation.throughputControl.controller.IThroughputController;
import com.azure.cosmos.implementation.throughputControl.controller.request.PkRangesThroughputRequestController;
import com.azure.cosmos.implementation.throughputControl.controller.request.IThroughputRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class ThroughputGroupControllerBase implements IThroughputController {
    private final static Logger logger = LoggerFactory.getLogger(ThroughputGroupControllerBase.class);
    private final Duration DEFAULT_THROUGHPUT_USAGE_RESET_DURATION = Duration.ofSeconds(1);

    private final ConnectionMode connectionMode;
    private final ThroughputControlGroup group;
    private final AtomicReference<Double> groupThroughput;
    private final AtomicReference<Integer> maxContainerThroughput;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final String targetContainerRid;

    private final CancellationTokenSource cancellationTokenSource;
    private final Scheduler scheduler;

    private final AsyncCache<String, IThroughputRequestController> requestControllerAsyncCache;

    public ThroughputGroupControllerBase(
        ConnectionMode connectionMode,
        ThroughputControlGroup group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid) {

        checkNotNull(group, "Throughput control group can not be null");
        checkNotNull(partitionKeyRangeCache, "Partition key range cache can not be null or empty");
        checkArgument(StringUtils.isNotEmpty(targetContainerRid), "Target container rid cannot be null nor empty");

        this.connectionMode = connectionMode;
        this.group = group;

        if (this.group.getTargetThroughputThreshold() != null) {
            checkNotNull(maxContainerThroughput, "Max container throughput can not be null when target throughput threshold defined");
        }
        this.maxContainerThroughput = new AtomicReference<>(maxContainerThroughput);

        this.groupThroughput = new AtomicReference<>(this.calculateGroupThroughput()); // Initialize after maxContainerThroughput
        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.targetContainerRid = targetContainerRid;

        this.cancellationTokenSource = new CancellationTokenSource();
        this.scheduler = Schedulers.elastic();

        this.requestControllerAsyncCache = new AsyncCache<>();
    }

    double calculateGroupThroughput() {
        double allocatedThroughput = Double.MAX_VALUE;
        if (this.group.getTargetThroughputThreshold() != null) {
            allocatedThroughput = Math.min(allocatedThroughput, this.maxContainerThroughput.get() * this.group.getTargetThroughputThreshold());
        }

        if (this.group.getTargetThroughput() != null) {
            allocatedThroughput = Math.min(allocatedThroughput, this.group.getTargetThroughput());
        }

        return allocatedThroughput;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.resolveRequestController()
            .doOnSuccess(dummy -> scheduler.schedule(() -> this.throughputUsageCycleRenewTask(this.cancellationTokenSource.getToken()).subscribe()))
            .thenReturn((T)this);
    }

    private Flux<Void> throughputUsageCycleRenewTask(CancellationToken cancellationToken) {
        return Mono.just(this)
            .delayElement(DEFAULT_THROUGHPUT_USAGE_RESET_DURATION)
            .then(this.resolveRequestController())
            .flatMap(requestController -> requestController.renewThroughputUsageCycle(this.groupThroughput.get()))
            .onErrorResume(throwable -> {
                logger.warn("Reset throughput usage failed with reason %s", throwable);
                return Mono.empty();
            }).repeat(() -> !cancellationToken.isCancellationRequested());
    }

    private Mono<IThroughputRequestController> createAndInitializeRequestController() {
        IThroughputRequestController requestController;
        if (this.connectionMode == ConnectionMode.DIRECT) {
            requestController = new PkRangesThroughputRequestController(
                this.partitionKeyRangeCache,
                this.targetContainerRid,
                this.groupThroughput.get());

        } else if (this.connectionMode == ConnectionMode.GATEWAY) {
            requestController = new GlobalThroughputRequestController(this.groupThroughput.get());
        } else {
            throw new IllegalArgumentException(String.format("Connection mode %s is not supported"));
        }

        return requestController.init();
    }

    public boolean isUseByDefault() {
        return this.group.isUseByDefault();
    }

    public Mono<Void> onContainerMaxThroughputRefresh(int maxContainerThroughput) {
        return Mono.just(maxContainerThroughput)
            .doOnNext(throughput ->
            {
                if (maxContainerThroughput != this.maxContainerThroughput.get()) {
                    this.maxContainerThroughput.set(throughput);
                    this.groupThroughput.set(calculateGroupThroughput());
                }
            })
            .then();
    }

    @Override
    public Mono<Void> close() {
        this.cancellationTokenSource.cancel();
        return Mono.empty();
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        return this.resolveRequestController()
            .flatMap(requestController -> {
                if (requestController.canHandleRequest(request)) {
                    return Mono.just(requestController);
                } else {
                    // We can not find the matching pkRange, it is because either the group control is out of sync
                    // or the request has staled info.
                    // We will handle the first scenario by creating a new request controller,
                    // while for second scenario, we will go to the original request mono, which will eventually get exception from server
                    return this.shouldUpdateRequestController(request)
                        .flatMap(shouldUpdate -> {
                            if (shouldUpdate) {
                                requestController.close();
                                this.refreshRequestController();
                                return this.resolveRequestController();
                            } else {
                                return Mono.just(requestController);
                            }
                        });
                }
            })
            .flatMap(requestController -> {
                if (requestController.canHandleRequest(request)) {
                    return requestController.processRequest(request, nextRequestMono)
                        .doOnError(throwable -> this.doOnError(throwable));
                } else {
                    // If we reach here and still can not handle the request, it should mean the request has staled info
                    // and the request will fail by server
                    return nextRequestMono;
                }
            });
    }

    private Mono<Boolean> shouldUpdateRequestController(RxDocumentServiceRequest request) {
        return this.partitionKeyRangeCache.tryGetRangeByPartitionKeyRangeId(
                null, request.requestContext.resolvedCollectionRid, request.requestContext.resolvedPartitionKeyRange.getId(), null)
            .map(pkRangeHolder -> pkRangeHolder.v)
            .flatMap(pkRange -> {
                if (pkRange == null) {
                    return Mono.just(Boolean.FALSE);
                } else {
                    return Mono.just(Boolean.TRUE);
                }});
    }

    private Mono<IThroughputRequestController> resolveRequestController() {
        return this.requestControllerAsyncCache.getAsync(
            this.group.getGroupName(),
            null,
            () -> this.createAndInitializeRequestController());
    }

    private void refreshRequestController() {
        this.requestControllerAsyncCache.refresh(
            this.group.getGroupName(),
            () -> this.createAndInitializeRequestController());
    }

    private void doOnError(Throwable throwable) {
        checkNotNull(throwable, "Throwable can not be null");

        CosmosException cosmosException = Utils.as(throwable, CosmosException.class);
        if (Exceptions.isPartitionSplit(cosmosException) || Exceptions.isPartitionCompletingSplittingException(cosmosException)) {
            this.refreshRequestController();
        }
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        return this.isUseByDefault() || StringUtils.equals(this.group.getGroupName(), request.getThroughputControlGroupName());
    }
}
