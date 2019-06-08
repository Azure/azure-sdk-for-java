/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos.changefeed.internal;

import com.microsoft.azure.cosmos.changefeed.CancellationToken;
import com.microsoft.azure.cosmos.changefeed.CancellationTokenSource;
import com.microsoft.azure.cosmos.changefeed.Lease;
import com.microsoft.azure.cosmos.changefeed.LeaseContainer;
import com.microsoft.azure.cosmos.changefeed.PartitionController;
import com.microsoft.azure.cosmos.changefeed.PartitionLoadBalancer;
import com.microsoft.azure.cosmos.changefeed.PartitionLoadBalancingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Implementation for {@link PartitionLoadBalancer}.
 */
public class PartitionLoadBalancerImpl implements PartitionLoadBalancer {
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
                this.partitionController.shutdown().block();
                this.cancellationTokenSource = null;
            }
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
