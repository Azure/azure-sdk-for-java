// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.LinkedCancellationToken;
import com.azure.cosmos.implementation.throughputControl.LinkedCancellationTokenSource;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
import com.azure.cosmos.implementation.throughputControl.controller.IThroughputController;
import com.azure.cosmos.implementation.throughputControl.controller.request.GlobalThroughputRequestController;
import com.azure.cosmos.implementation.throughputControl.controller.request.IThroughputRequestController;
import com.azure.cosmos.implementation.throughputControl.controller.request.PkRangesThroughputRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
 * 2. Schedule reset throughput usage every 1s.
 */
public abstract class ThroughputGroupControllerBase implements IThroughputController {
    private final static Logger logger = LoggerFactory.getLogger(ThroughputGroupControllerBase.class);
    private final Duration DEFAULT_THROUGHPUT_USAGE_RESET_DURATION = Duration.ofSeconds(1);

    private final ConnectionMode connectionMode;
    private final ThroughputControlGroupInternal group;
    private final AtomicInteger maxContainerThroughput;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final AsyncCache<String, IThroughputRequestController> requestControllerAsyncCache;
    private final String targetContainerRid;

    protected final AtomicReference<Double> groupThroughput;
    protected final LinkedCancellationTokenSource cancellationTokenSource;

    public ThroughputGroupControllerBase(
        ConnectionMode connectionMode,
        ThroughputControlGroupInternal group,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid,
        LinkedCancellationToken parentToken) {

        checkNotNull(group, "Throughput control group can not be null");
        checkNotNull(partitionKeyRangeCache, "Partition key range cache can not be null or empty");
        checkArgument(StringUtils.isNotEmpty(targetContainerRid), "Target container rid cannot be null nor empty");

        this.connectionMode = connectionMode;
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

        this.cancellationTokenSource = new LinkedCancellationTokenSource(parentToken);
    }

    public abstract double getClientAllocatedThroughput();

    public abstract void recordThroughputUsage(double loadFactor);

    protected void calculateGroupThroughput() {
        double allocatedThroughput = Double.MAX_VALUE;
        if (this.group.getTargetThroughputThreshold() != null) {
            allocatedThroughput = Math.min(allocatedThroughput, this.maxContainerThroughput.get() * this.group.getTargetThroughputThreshold());
        }

        if (this.group.getTargetThroughput() != null) {
            allocatedThroughput = Math.min(allocatedThroughput, this.group.getTargetThroughput());
        }

        this.groupThroughput.set(allocatedThroughput);
    }

    public Flux<Void> throughputUsageCycleRenewTask(LinkedCancellationToken cancellationToken) {
        checkNotNull(cancellationToken, "Cancellation token can not be null");
        return Mono.delay(DEFAULT_THROUGHPUT_USAGE_RESET_DURATION, CosmosSchedulers.COSMOS_PARALLEL)
            .flatMap(t -> {
                if (cancellationToken.isCancellationRequested()) {
                    return Mono.empty();
                } else {
                    return this.resolveRequestController();
                }
            })
            .doOnSuccess(requestController -> {
                if (requestController != null) {
                    this.recordThroughputUsage(requestController.renewThroughputUsageCycle(this.getClientAllocatedThroughput()));
                }
            })
            .onErrorResume(throwable -> {
                logger.warn("Reset throughput usage failed with reason ", throwable);
                return Mono.empty();
            })
            .then()
            .repeat(() -> !cancellationToken.isCancellationRequested());
    }

    private Mono<IThroughputRequestController> createAndInitializeRequestController() {
        IThroughputRequestController requestController;

        if (this.connectionMode == ConnectionMode.DIRECT) {
            requestController = new PkRangesThroughputRequestController(
                this.partitionKeyRangeCache,
                this.targetContainerRid,
                this.getClientAllocatedThroughput());

        } else if (this.connectionMode == ConnectionMode.GATEWAY) {
            requestController = new GlobalThroughputRequestController(this.getClientAllocatedThroughput());
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
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono) {
        return this.resolveRequestController()
            .flatMap(requestController -> {
                if (requestController.canHandleRequest(request)) {
                    return requestController.processRequest(request, originalRequestMono)
                        .doOnError(throwable -> this.handleException(throwable));
                }

                // We can not find the matching pkRange, it is because either the group control is out of sync
                // or the request has staled info.
                // We will handle the first scenario by creating a new request controller,
                // while for second scenario, we will go to the original request mono, which will eventually get exception from server
                return this.updateControllerAndRetry(request, originalRequestMono);
            });
    }

    private <T> Mono<T> updateControllerAndRetry(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {

        return this.shouldUpdateRequestController(request)
            .flatMap(shouldUpdate -> {
                if (shouldUpdate) {
                    this.refreshRequestController();
                    return this.resolveRequestController()
                        .flatMap(updatedController -> {
                            if (updatedController.canHandleRequest(request)) {
                                return updatedController.processRequest(request, nextRequestMono)
                                    .doOnError(throwable -> this.handleException(throwable));
                            } else {
                                // If we reach here and still can not handle the request, it should mean the request has staled info
                                // and the request will fail by server
                                logger.warn(
                                    "Can not find request controller to handle request {} with pkRangeId {}",
                                    request.getActivityId(),
                                    this.getResolvedPartitionKeyRangeId(request));
                                return nextRequestMono;
                            }
                        });
                } else {
                    return nextRequestMono;
                }
            });
    }

    private String getResolvedPartitionKeyRangeId(RxDocumentServiceRequest request) {
        if (request.requestContext != null && request.requestContext.resolvedPartitionKeyRange != null) {
            return request.requestContext.resolvedPartitionKeyRange.getId();
        }

        return StringUtils.EMPTY;
    }

    private Mono<Boolean> shouldUpdateRequestController(RxDocumentServiceRequest request) {
        return this.partitionKeyRangeCache.tryGetRangeByPartitionKeyRangeId(
                null, request.requestContext.resolvedCollectionRid, request.requestContext.resolvedPartitionKeyRange.getId(), null)
            .flatMap(pkRangeHolder -> {
                if (pkRangeHolder.v == null) {
                    return Mono.just(Boolean.FALSE);
                } else {
                    return Mono.just(Boolean.TRUE);
                }});
    }

    protected Mono<IThroughputRequestController> resolveRequestController() {
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
