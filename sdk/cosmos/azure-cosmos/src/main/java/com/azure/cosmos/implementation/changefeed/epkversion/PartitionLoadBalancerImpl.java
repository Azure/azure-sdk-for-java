// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.PartitionController;
import com.azure.cosmos.implementation.changefeed.PartitionLoadBalancer;
import com.azure.cosmos.implementation.changefeed.PartitionLoadBalancingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for {@link PartitionLoadBalancer}.
 */
class PartitionLoadBalancerImpl implements PartitionLoadBalancer {
    private final Logger logger = LoggerFactory.getLogger(PartitionLoadBalancerImpl.class);
    private final PartitionController partitionController;
    private final LeaseContainer leaseContainer;
    private final PartitionLoadBalancingStrategy partitionLoadBalancingStrategy;
    private final Duration leaseAcquireInterval;
    private final Scheduler scheduler;

    private CancellationTokenSource cancellationTokenSource;

    private volatile boolean started;

    private final Object lock;

    public PartitionLoadBalancerImpl(
            PartitionController partitionController,
            LeaseContainer leaseContainer,
            PartitionLoadBalancingStrategy partitionLoadBalancingStrategy,
            Duration leaseAcquireInterval,
            Scheduler scheduler) {

        checkNotNull(partitionController, "Argument 'partitionController' can not be null");
        checkNotNull(leaseContainer, "Argument 'leaseContainer' can not be null");
        checkNotNull(partitionLoadBalancingStrategy, "Argument 'partitionLoadBalancingStrategy' can not be null");
        checkNotNull(scheduler, "Argument 'scheduler' can not be null");


        this.partitionController = partitionController;
        this.leaseContainer = leaseContainer;
        this.partitionLoadBalancingStrategy = partitionLoadBalancingStrategy;
        this.leaseAcquireInterval = leaseAcquireInterval;
        this.scheduler = scheduler;

        this.started = false;
        this.lock = new Object();
    }

    @Override
    public Mono<Void> start() {
        synchronized (lock) {
            if (this.started) {
                throw new IllegalStateException("Partition load balancer already started");
            }

            this.cancellationTokenSource = new CancellationTokenSource();
            this.started = true;
        }

        return Mono.fromRunnable( () -> {
            scheduler.schedule(() -> this.run(this.cancellationTokenSource.getToken()).subscribe());
        });
    }

    @Override
    public Mono<Void> stop() {
        synchronized (lock) {
            this.started = false;
            this.cancellationTokenSource.cancel();
        }

        return this.partitionController.shutdown();
    }

    @Override
    public boolean isRunning() {
        return this.started;
    }

    private Mono<Void> run(CancellationToken cancellationToken) {
        return Flux.just(this)
            .flatMap(value -> this.leaseContainer.getAllLeases())
            .collectList()
            .flatMap(allLeases -> {
                if (cancellationToken.isCancellationRequested()) return Mono.empty();
                List<Lease> leasesToTake = this.partitionLoadBalancingStrategy.selectLeasesToTake(allLeases);
                if (leasesToTake.size() > 0) {
                    this.logger.info("Found {} total leases, taking ownership of {}", allLeases.size(), leasesToTake.size());
                }

                if (cancellationToken.isCancellationRequested()) return Mono.empty();
                return Flux.fromIterable(leasesToTake)
                    .limitRate(1)
                    .flatMap(lease -> {
                        if (cancellationToken.isCancellationRequested()) return Mono.empty();
                        return this.partitionController.addOrUpdateLease(lease);
                    })
                    .then();
            })
            .onErrorResume(throwable -> {
                // "catch all" exception handler to keep the loop going until the user stops the change feed processor
                logger.warn("Unexpected exception thrown while trying to acquire available leases", throwable);
                return Mono.empty();
            })
            .then(
                Mono.just(this)
                    .flatMap(value -> {
                        if (cancellationToken.isCancellationRequested()) {
                            return Mono.empty();
                        }
                        Instant stopTimer = Instant.now().plus(this.leaseAcquireInterval);
                        return Mono.just(value)
                            .delayElement(Duration.ofMillis(100), CosmosSchedulers.COSMOS_PARALLEL)
                            .repeat(() -> {
                                Instant currentTime = Instant.now();
                                return !cancellationToken.isCancellationRequested() && currentTime.isBefore(stopTimer);
                            })
                            .then();
                    })
            )
            .repeat(() -> !cancellationToken.isCancellationRequested())
            .then()
            .onErrorResume(throwable -> {
                // We should not get here.
                logger.info("Partition load balancer task stopped.");
                return this.stop();
            });
    }
}
