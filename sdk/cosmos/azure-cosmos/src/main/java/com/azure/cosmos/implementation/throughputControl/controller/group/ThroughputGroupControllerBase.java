// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.throughputControl.controller.IThroughputController;
import com.azure.cosmos.implementation.throughputControl.controller.request.GlobalThroughputRequestController;
import com.azure.cosmos.implementation.throughputControl.controller.request.IThroughputRequestController;
import com.azure.cosmos.implementation.throughputControl.controller.request.PkRangesThroughputRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.Exceptions.isPartitionCompletingSplittingException;
import static com.azure.cosmos.implementation.Exceptions.isPartitionSplit;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Throughput group controller. Two common tasks across all group controller implementations:
 * 1. Create and initialize request controller based on connection mode
 * 2. Schedule reset throughput uage every 1s.
 */
public abstract class ThroughputGroupControllerBase implements IThroughputController {
    private final static Logger logger = LoggerFactory.getLogger(ThroughputGroupControllerBase.class);
    private final Duration DEFAULT_THROUGHPUT_USAGE_RESET_DURATION = Duration.ofSeconds(1);

    private final ConnectionMode connectionMode;
    private final GlobalEndpointManager globalEndpointManager;
    private final ThroughputControlGroup group;
    private final AtomicReference<Double> groupThroughput;
    private final AtomicInteger maxContainerThroughput;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final AsyncCache<String, IThroughputRequestController> requestControllerAsyncCache;
    private final String targetContainerRid;

    private final CancellationTokenSource cancellationTokenSource;

    public ThroughputGroupControllerBase(
        ConnectionMode connectionMode,
        GlobalEndpointManager globalEndpointManager,
        ThroughputControlGroup group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid) {

        checkNotNull(globalEndpointManager, "Global endpoint manager can not be null");
        checkNotNull(group, "Throughput control group can not be null");
        checkNotNull(partitionKeyRangeCache, "Partition key range cache can not be null or empty");
        checkArgument(StringUtils.isNotEmpty(targetContainerRid), "Target container rid cannot be null nor empty");

        this.connectionMode = connectionMode;
        this.globalEndpointManager = globalEndpointManager;
        this.group = group;

        if (this.group.getTargetThroughputThreshold() != null) {
            checkNotNull(maxContainerThroughput, "Max container throughput can not be null when target throughput threshold defined");
            this.maxContainerThroughput = new AtomicInteger(maxContainerThroughput);
        } else {
            this.maxContainerThroughput = null;
        }

        this.groupThroughput = new AtomicReference<>();
        this.calculateGroupThroughput();// Calculate after maxContainerThroughput

        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.requestControllerAsyncCache = new AsyncCache<>();
        this.targetContainerRid = targetContainerRid;

        this.cancellationTokenSource = new CancellationTokenSource();
    }

    private void calculateGroupThroughput() {
        double allocatedThroughput = Double.MAX_VALUE;
        if (this.group.getTargetThroughputThreshold() != null) {
            allocatedThroughput = Math.min(allocatedThroughput, this.maxContainerThroughput.get() * this.group.getTargetThroughputThreshold());
        }

        if (this.group.getTargetThroughput() != null) {
            allocatedThroughput = Math.min(allocatedThroughput, this.group.getTargetThroughput());
        }

        this.groupThroughput.set(allocatedThroughput);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.resolveRequestController()
            .doOnSuccess(dummy -> {
                this.throughputUsageCycleRenewTask(this.cancellationTokenSource.getToken()).subscribeOn(Schedulers.parallel()).subscribe();
            })
            .thenReturn((T)this);
    }

    private Flux<Void> throughputUsageCycleRenewTask(CancellationToken cancellationToken) {
        checkNotNull(cancellationToken, "Cancellation token can not be null");
        return Mono.delay(DEFAULT_THROUGHPUT_USAGE_RESET_DURATION)
            .flatMap(t -> this.resolveRequestController())
            .doOnSuccess(requestController -> requestController.renewThroughputUsageCycle(this.groupThroughput.get()))
            .onErrorResume(throwable -> {
                logger.warn("Reset throughput usage failed with reason", throwable);
                return Mono.empty();
            })
            .then()
            .repeat(() -> !cancellationToken.isCancellationRequested());
    }

    private Mono<IThroughputRequestController> createAndInitializeRequestController() {
        IThroughputRequestController requestController;
        if (this.connectionMode == ConnectionMode.DIRECT) {
            requestController = new PkRangesThroughputRequestController(
                this.globalEndpointManager,
                this.partitionKeyRangeCache,
                this.targetContainerRid,
                this.groupThroughput.get());

        } else if (this.connectionMode == ConnectionMode.GATEWAY) {
            requestController = new GlobalThroughputRequestController(this.globalEndpointManager, this.groupThroughput.get());
        } else {
            throw new IllegalArgumentException(String.format("Connection mode %s is not supported", this.connectionMode));
        }

        return requestController.init();
    }

    public boolean isDefault() {
        return this.group.isDefault();
    }

    public void onContainerMaxThroughputRefresh(int maxContainerThroughput) {
        if (this.maxContainerThroughput.getAndSet(maxContainerThroughput) != maxContainerThroughput) {
            this.calculateGroupThroughput();
        }
    }

    @Override
    public Mono<Void> close() {
        this.cancellationTokenSource.cancel();
        return this.resolveRequestController()
            .flatMap(requestController -> requestController.close());
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
                                requestController.close().subscribeOn(Schedulers.parallel()).subscribe();
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
                        .doOnError(throwable -> this.handleException(throwable));
                } else {
                    // If we reach here and still can not handle the request, it should mean the request has staled info
                    // and the request will fail by server
                    logger.warn(
                        "Can not find request controller to handle request {} with pkRangeId {}",
                        request.getActivityId(),
                        request.requestContext.resolvedPartitionKeyRange.getId());
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

    private void handleException(Throwable throwable) {
        checkNotNull(throwable, "Throwable can not be null");

        CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
        if (isPartitionSplit(cosmosException) || isPartitionCompletingSplittingException(cosmosException)) {
            this.refreshRequestController();
        }
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        return this.isDefault() || StringUtils.equals(this.group.getGroupName(), request.getThroughputControlGroupName());
    }
}
