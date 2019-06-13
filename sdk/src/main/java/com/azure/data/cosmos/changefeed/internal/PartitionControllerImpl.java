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
package com.azure.data.cosmos.changefeed.internal;

import com.azure.data.cosmos.changefeed.CancellationToken;
import com.azure.data.cosmos.changefeed.CancellationTokenSource;
import com.azure.data.cosmos.changefeed.Lease;
import com.azure.data.cosmos.changefeed.LeaseContainer;
import com.azure.data.cosmos.changefeed.LeaseManager;
import com.azure.data.cosmos.changefeed.PartitionController;
import com.azure.data.cosmos.changefeed.PartitionSupervisor;
import com.azure.data.cosmos.changefeed.PartitionSupervisorFactory;
import com.azure.data.cosmos.changefeed.PartitionSynchronizer;
import com.azure.data.cosmos.changefeed.exceptions.PartitionSplitException;
import com.azure.data.cosmos.changefeed.exceptions.TaskCancelledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Implementation for {@link PartitionController}.
 */
public class PartitionControllerImpl implements PartitionController {
    private final Logger logger = LoggerFactory.getLogger(PartitionControllerImpl.class);
    //    private final Map<STRING, Thread> currentlyOwnedPartitions = new ConcurrentHashMap<STRING, Thread>();
    private final Map<String, WorkerTask> currentlyOwnedPartitions = new ConcurrentHashMap<>();

    private final LeaseContainer leaseContainer;
    private final LeaseManager leaseManager;
    private final PartitionSupervisorFactory partitionSupervisorFactory;
    private final PartitionSynchronizer synchronizer;
    private CancellationTokenSource shutdownCts;

    private final ExecutorService executorService;

    public PartitionControllerImpl(
        LeaseContainer leaseContainer,
        LeaseManager leaseManager,
        PartitionSupervisorFactory partitionSupervisorFactory,
        PartitionSynchronizer synchronizer,
        ExecutorService executorService) {

        this.leaseContainer = leaseContainer;
        this.leaseManager = leaseManager;
        this.partitionSupervisorFactory = partitionSupervisorFactory;
        this.synchronizer = synchronizer;
        this.executorService = executorService;
    }

    @Override
    public Mono<Void> initialize() {
        this.shutdownCts = new CancellationTokenSource();
        return this.loadLeases();
    }

    @Override
    public synchronized Mono<Lease> addOrUpdateLease(Lease lease) {
        WorkerTask workerTask = this.currentlyOwnedPartitions.get(lease.getLeaseToken());
        if ( workerTask != null && workerTask.isRunning()) {
            Lease updatedLease = this.leaseManager.updateProperties(lease).block();
            logger.debug(String.format("Partition %s: updated.", lease.getLeaseToken()));
            return Mono.just(updatedLease);
        }

        try {
            Lease updatedLease = this.leaseManager.acquire(lease).block();
            if (updatedLease != null) lease = updatedLease;

            logger.info(String.format("Partition %s: acquired.", lease.getLeaseToken()));
        } catch (RuntimeException ex) {
            this.removeLease(lease).block();
            throw ex;
        }

        PartitionSupervisor supervisor = this.partitionSupervisorFactory.create(lease);
        this.currentlyOwnedPartitions.put(lease.getLeaseToken(), this.processPartition(supervisor, lease));

        return Mono.just(lease);
    }

    @Override
    public Mono<Void> shutdown() {
        // TODO: wait for the threads to finish.
        this.shutdownCts.cancel();
//        this.currentlyOwnedPartitions.clear();

        return Mono.empty();
    }

    private Mono<Void> loadLeases() {
        PartitionControllerImpl self = this;
        logger.debug("Starting renew leases assigned to this host on initialize.");

        return this.leaseContainer.getOwnedLeases()
            .flatMap( lease -> {
                logger.info(String.format("Acquired lease for PartitionId '%s' on startup.", lease.getLeaseToken()));
                return self.addOrUpdateLease(lease);
            }).then();
    }

    private Mono<Void> removeLease(Lease lease) {
        return Mono.fromRunnable(() -> {
            if (this.currentlyOwnedPartitions.get(lease.getLeaseToken()) != null) {
                WorkerTask workerTask = this.currentlyOwnedPartitions.remove(lease.getLeaseToken());

                if (workerTask.isRunning()) {
                    workerTask.interrupt();
                }

                logger.info(String.format("Partition %s: released.", lease.getLeaseToken()));

                try {
                    this.leaseManager.release(lease).block();
                } catch (Exception e) {
                    logger.warn(String.format("Partition %s: failed to remove lease.", lease.getLeaseToken()), e);
                } finally {
                    // TODO: Stop the corresponding threads.
                }
            }
        });
    }

    private WorkerTask processPartition(PartitionSupervisor partitionSupervisor, Lease lease) {
        PartitionControllerImpl self = this;

        CancellationToken cancellationToken = this.shutdownCts.getToken();

        WorkerTask partitionSupervisorTask = new WorkerTask(Mono.fromRunnable( () -> {
            try {
                partitionSupervisor.run(cancellationToken).block();
            } catch (PartitionSplitException ex) {
                self.handleSplit(lease, ex.getLastContinuation()).block();
            } catch (TaskCancelledException tcex) {
                logger.debug(String.format("Partition %s: processing canceled.", lease.getLeaseToken()));
            } catch (Exception e) {
                logger.warn(String.format("Partition %s: processing failed.", lease.getLeaseToken()), e);
            }

            self.removeLease(lease).block();
        }));

        this.executorService.execute(partitionSupervisorTask);

        return partitionSupervisorTask;
    }

    private Mono<Void> handleSplit(Lease lease, String lastContinuationToken) {
        PartitionControllerImpl self = this;

        lease.setContinuationToken(lastContinuationToken);
        return this.synchronizer.splitPartition(lease)
            .flatMap(l -> {
                l.setProperties(lease.getProperties());
                return self.addOrUpdateLease(l);
            }).then(self.leaseManager.delete(lease))
            .onErrorResume(throwable -> {
                logger.warn(String.format("partition %s: failed to split", lease.getLeaseToken()), throwable);
                return  Mono.empty();
            });
    }
}
