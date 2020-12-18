// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget.controller.group;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.throughputBudget.ThroughputBudgetControlContainerManager;
import com.azure.cosmos.implementation.throughputBudget.ThroughputBudgetGroupConfigInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ThroughputBudgetGroupDistributedController extends ThroughputBudgetGroupControllerBase {

    private static Logger logger = LoggerFactory.getLogger(ThroughputBudgetGroupDistributedController.class);
    private final CosmosAsyncContainer targetControllerContainer;
    private final Duration documentRenewalInterval;
    private final ThroughputBudgetControlContainerManager containerManager;

    public ThroughputBudgetGroupDistributedController(
        ConnectionMode connectionMode,
        ThroughputBudgetGroupConfigInternal groupConfig,
        Integer maxContainerThroughput,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        super(connectionMode, groupConfig, maxContainerThroughput, partitionKeyRangeCache);
        this.targetControllerContainer = groupConfig.getDistributedControlConfig().getControllerContainer();
        this.documentRenewalInterval = groupConfig.getDistributedControlConfig().getDocumentRenewalInterval();

        this.containerManager = new ThroughputBudgetControlContainerManager(
            this.targetControllerContainer,
            groupConfig);
    }

    @Override
    public Mono<ThroughputBudgetGroupControllerBase> init() {
        return super.init()
            .then(this.containerManager.validateControllerContainer())
            .then(this.containerManager.validateGroupConfigItem())
            .then(this.containerManager.createGroupClientItem(1.0))
            .then(Mono.just(this));
    }

    @Override
    public Mono<Void> close() {
        return super.close();
    }

    @Override
    Flux<Void> calculateThroughputTask(CancellationToken cancellationToken) {
        return Mono.just(this)
            .delayElement(documentRenewalInterval)
            .then(
                Mono.just(this.loadFactorHistory)
                    .flatMap(loadFactors -> Mono.just(loadFactors.stream().mapToDouble(Double::doubleValue).average()))
                    .flatMap(avgLoadFactor -> {
                        return this.containerManager.upsertGroupClientItem(avgLoadFactor.getAsDouble())
                            .flatMap(avoid -> this.containerManager.queryClientLoads())
                            .flatMap(totalLoads -> Mono.just((avgLoadFactor .getAsDouble()/ totalLoads)))
                            .doOnSuccess(value -> logger.info("Update successfully"));
                    })
                    .flatMap(assignedLoad -> {
                        this.throughputPercentage.set(assignedLoad);
                        this.groupThroughput.set(this.calculateGroupThroughput());
                        return Mono.empty();
                    })
            )
            .doOnSuccess(avoid -> this.loadFactorHistory.clear())
            .onErrorResume(throwable -> {
                logger.warn("Re calculate throughput task failed for reason ", throwable);
                return Mono.empty();
            })
            .then()
            .repeat(() -> !cancellationToken.isCancellationRequested());
    }
}
