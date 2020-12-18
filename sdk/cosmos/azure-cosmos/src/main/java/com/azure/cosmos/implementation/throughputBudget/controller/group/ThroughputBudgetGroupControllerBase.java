// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget.controller.group;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.throughputBudget.ThroughputBudgetGroupConfigInternal;
import com.azure.cosmos.implementation.throughputBudget.controller.GlobalThroughputBudgetRequestAuthorizerController;
import com.azure.cosmos.implementation.throughputBudget.controller.IThroughputBudgetController;
import com.azure.cosmos.implementation.throughputBudget.controller.PkRangesThroughputBudgetRequestAuthorizerController;
import com.azure.cosmos.implementation.throughputBudget.controller.ThroughputBudgetRequestAuthorizerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class ThroughputBudgetGroupControllerBase implements IThroughputBudgetController {
    private final static Logger logger = LoggerFactory.getLogger(ThroughputBudgetGroupControllerBase.class);
    final Duration DEFAULT_THROUGHPUT_USAGE_RESET_DURATION = Duration.ofSeconds(1);

    final ConnectionMode connectionMode;
    final ThroughputBudgetGroupConfigInternal groupConfig;
    final AtomicReference<Double> groupThroughput;
    final AtomicReference<Integer> maxContainerThroughput;
    final RxPartitionKeyRangeCache partitionKeyRangeCache;

    final Scheduler scheduler;
    final CancellationTokenSource cancellationTokenSource;

    ThroughputBudgetRequestAuthorizerController requestAuthorizerController;

    final List<Double> loadFactorHistory;
    final AtomicReference<Double> throughputPercentage;

    public ThroughputBudgetGroupControllerBase(
        ConnectionMode connectionMode,
        ThroughputBudgetGroupConfigInternal groupConfig,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        checkNotNull(groupConfig, "Group config can not be null");
        checkNotNull(partitionKeyRangeCache, "Partition key range cache can not be null or empty");

        this.connectionMode = connectionMode;
        this.groupConfig = groupConfig;
        this.throughputPercentage = new AtomicReference<>(1.0);

        this.maxContainerThroughput = new AtomicReference<>(maxContainerThroughput);
        this.groupThroughput = new AtomicReference(this.calculateGroupThroughput());

        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.requestAuthorizerController = this.createRequestAuthorizerController();
        this.cancellationTokenSource = new CancellationTokenSource();
        this.scheduler = Schedulers.elastic();

        this.loadFactorHistory = new ArrayList<>();
    }

    public Mono<ThroughputBudgetGroupControllerBase> init() {
        return this.requestAuthorizerController.init(this.groupThroughput.get())
            .doOnSuccess(dummy -> {
                scheduler.schedule(() -> this.resetThroughputTask(this.cancellationTokenSource.getToken()));
                scheduler.schedule(() -> this.calculateThroughputTask(this.cancellationTokenSource.getToken()));
            })
            .then(Mono.just(this));
    }

    public boolean isUseByDefault() {
        return this.groupConfig.isUseByDefault();
    }

    public Mono<Void> onMaxContainerThroughputRefresh(Integer maxContainerThroughput) {
        return Mono.justOrEmpty(maxContainerThroughput)
            .doOnNext(throughput ->
            {
                this.maxContainerThroughput.set(throughput);
                this.groupThroughput.set(calculateGroupThroughput());
            })
            .then();
    }

    @Override
    public Mono<Void> close() {
        this.cancellationTokenSource.cancel();
        return this.requestAuthorizerController.close();
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        if (this.requestAuthorizerController.canHandleRequest(request)) {
            return this.requestAuthorizerController.processRequest(request, nextRequestMono);
        } else {
            // TODO: check whethe the pkRange still exists
            return this.partitionKeyRangeCache.tryGetRangeByPartitionKeyRangeId(null, request.requestContext.resolvedCollectionRid, request.requestContext.resolvedPartitionKeyRange.getId(), null)
                .map(pkRangeHolder -> pkRangeHolder.v)
                .flatMap(pkRange -> {
                    if (pkRange == null) {
                        return nextRequestMono;
                    } else {
                        return this.createRequestAuthorizerController().init(this.groupThroughput.get())
                            .doOnSuccess(controller -> this.requestAuthorizerController = controller)
                            .then(this.requestAuthorizerController.processRequest(request, nextRequestMono));
                    }
                });
        }
    }

    double calculateGroupThroughput() {
        double allocatedThroughput = Double.MAX_VALUE;
        if (this.groupConfig.getThroughputLimitThreshold() != null) {
            allocatedThroughput = Math.min(allocatedThroughput, this.maxContainerThroughput.get() * this.groupConfig.getThroughputLimitThreshold());
        }
        if (this.groupConfig.getThroughputLimit() != null) {
            allocatedThroughput = Math.min(allocatedThroughput, this.groupConfig.getThroughputLimit());
        }

        return allocatedThroughput * this.throughputPercentage.get();
    }

    private ThroughputBudgetRequestAuthorizerController createRequestAuthorizerController() {
        if (this.connectionMode == ConnectionMode.DIRECT) {
            return new PkRangesThroughputBudgetRequestAuthorizerController(
                this.partitionKeyRangeCache,
                this.groupConfig.getTargetContainerRid());
        } else if (this.connectionMode == ConnectionMode.GATEWAY) {
            return new GlobalThroughputBudgetRequestAuthorizerController();
        }

        throw new IllegalArgumentException(String.format("Connection mode %s is not supported"));
    }

    public Flux<Void> resetThroughputTask(CancellationToken cancellationToken) {
        return Mono.just(this)
            .delayElement(DEFAULT_THROUGHPUT_USAGE_RESET_DURATION)
            .then(
                this.requestAuthorizerController.calculateLoadFactor()
                    .doOnSuccess(loadFactor -> this.loadFactorHistory.add(loadFactor))
            )
            .then(this.requestAuthorizerController.resetThroughput(this.groupThroughput.get()))
            .onErrorResume(throwable -> {
                logger.warn("Reset throughput usage failed with reason %s", throwable);
                return Mono.empty();
            }).repeat(() -> !cancellationToken.isCancellationRequested());
    }

    abstract Flux<Void> calculateThroughputTask(CancellationToken cancellationToken);
}
