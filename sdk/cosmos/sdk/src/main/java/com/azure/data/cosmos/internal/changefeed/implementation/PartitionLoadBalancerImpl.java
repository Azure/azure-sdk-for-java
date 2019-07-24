// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.CancellationToken;
import com.azure.data.cosmos.internal.changefeed.CancellationTokenSource;
import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.LeaseContainer;
import com.azure.data.cosmos.internal.changefeed.PartitionController;
import com.azure.data.cosmos.internal.changefeed.PartitionLoadBalancer;
import com.azure.data.cosmos.internal.changefeed.PartitionLoadBalancingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Implementation for {@link PartitionLoadBalancer}.
 */
class PartitionLoadBalancerImpl implements PartitionLoadBalancer {
    private final Logger logger = LoggerFactory.getLogger(PartitionLoadBalancerImpl.class);
    private final PartitionController partitionController;
    private final LeaseContainer leaseContainer;
    private final PartitionLoadBalancingStrategy partitionLoadBalancingStrategy;
    private final Duration leaseAcquireInterval;
    private final ExecutorService executorService;

    private CancellationTokenSource cancellationTokenSource;

    private volatile boolean started;

    private final Object lock;

    public PartitionLoadBalancerImpl(
        PartitionController partitionController,
        LeaseContainer leaseContainer,
        PartitionLoadBalancingStrategy partitionLoadBalancingStrategy,
        Duration leaseAcquireInterval,
        ExecutorService executorService) {

        if (partitionController == null) throw new IllegalArgumentException("partitionController");
        if (leaseContainer == null) throw new IllegalArgumentException("leaseContainer");
        if (partitionLoadBalancingStrategy == null) throw new IllegalArgumentException("partitionLoadBalancingStrategy");
        if (executorService == null) throw new IllegalArgumentException("executorService");

        this.partitionController = partitionController;
        this.leaseContainer = leaseContainer;
        this.partitionLoadBalancingStrategy = partitionLoadBalancingStrategy;
        this.leaseAcquireInterval = leaseAcquireInterval;
        this.executorService = executorService;

        this.started = false;
        this.lock = new Object();
    }

    @Override
    public Mono<Void> start() {
        PartitionLoadBalancerImpl self = this;

        return Mono.fromRunnable( () -> {
            synchronized (lock) {
                if (this.started) {
                    throw new IllegalStateException("Partition load balancer already started");
                }

                this.started = true;
                this.cancellationTokenSource = new CancellationTokenSource();
            }

            CancellationToken cancellationToken = this.cancellationTokenSource.getToken();

            this.executorService.execute(() -> self.run(cancellationToken).block());
        });
    }

    @Override
    public Mono<Void> stop() {
        return Mono.fromRunnable( () -> {
            synchronized (lock) {
                this.started = false;
                this.cancellationTokenSource.cancel();
            }

            this.partitionController.shutdown().block();
            this.cancellationTokenSource = null;
        });
    }

    private Mono<Void> run(CancellationToken cancellationToken) {
        PartitionLoadBalancerImpl self = this;

        return Mono.fromRunnable( () -> {
            try {
                while (!cancellationToken.isCancellationRequested()) {
                    List<Lease> allLeases = self.leaseContainer.getAllLeases().collectList().block();
                    List<Lease> leasesToTake = self.partitionLoadBalancingStrategy.selectLeasesToTake(allLeases);
                    for (Lease lease : leasesToTake) {
                        self.partitionController.addOrUpdateLease(lease).block();
                    }

                    long remainingWork = this.leaseAcquireInterval.toMillis();

                    try {
                        while (!cancellationToken.isCancellationRequested() && remainingWork > 0) {
                            Thread.sleep(100);
                            remainingWork -= 100;
                        }
                    } catch (InterruptedException ex) {
                        // exception caught
                        logger.warn("Partition load balancer caught an interrupted exception", ex);
                    }
                }
            } catch (Exception ex) {
                // We should not get here.
                logger.info("Partition load balancer task stopped.");
                this.stop();
            }
        });
    }
}
