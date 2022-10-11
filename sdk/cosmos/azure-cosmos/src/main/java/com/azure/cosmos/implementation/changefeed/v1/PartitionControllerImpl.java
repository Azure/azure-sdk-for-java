// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.v1;

import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.PartitionController;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisor;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisorFactory;
import com.azure.cosmos.implementation.changefeed.PartitionSynchronizer;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation for {@link PartitionController}.
 */
class PartitionControllerImpl implements PartitionController {
    private static final Logger logger = LoggerFactory.getLogger(PartitionControllerImpl.class);
    private final Map<String, WorkerTask> currentlyOwnedPartitions = new ConcurrentHashMap<>();

    private final LeaseContainer leaseContainer;
    private final LeaseManager leaseManager;
    private final PartitionSupervisorFactory partitionSupervisorFactory;
    private final PartitionSynchronizer synchronizer;
    private CancellationTokenSource shutdownCts;

    private final Scheduler scheduler;

    public PartitionControllerImpl(
            LeaseContainer leaseContainer,
            LeaseManager leaseManager,
            PartitionSupervisorFactory partitionSupervisorFactory,
            PartitionSynchronizer synchronizer,
            Scheduler scheduler) {

        this.leaseContainer = leaseContainer;
        this.leaseManager = leaseManager;
        this.partitionSupervisorFactory = partitionSupervisorFactory;
        this.synchronizer = synchronizer;
        this.scheduler = scheduler;
    }

    @Override
    public Mono<Void> initialize() {
        this.shutdownCts = new CancellationTokenSource();
        return this.loadLeases();
    }

    @Override
    public synchronized Mono<Lease> addOrUpdateLease(final Lease lease) {
        WorkerTask workerTask = this.currentlyOwnedPartitions.get(lease.getLeaseToken());
        if (workerTask != null && workerTask.isRunning()) {
            return this.leaseManager.updateProperties(lease)
                .map(updatedLease -> {
                    logger.debug("Lease with token {}: updated.", updatedLease.getLeaseToken());
                    return updatedLease;
                });
        }

        return this.leaseManager.acquire(lease)
            .map(updatedLease -> {
                WorkerTask checkTask = this.currentlyOwnedPartitions.get(lease.getLeaseToken());
                if (checkTask == null) {
                    logger.info("Lease with token {}: acquired.", updatedLease.getLeaseToken());
                    PartitionSupervisor supervisor = this.partitionSupervisorFactory.create(updatedLease);
                    this.currentlyOwnedPartitions.put(updatedLease.getLeaseToken(), this.processPartition(supervisor, updatedLease));
                }
                return updatedLease;
            })
            .onErrorResume(throwable -> {
                logger.warn("Lease with token {}: unexpected error; removing lease from current cache.", lease.getLeaseToken());
                return this.removeLease(lease).then(Mono.error(throwable));
            });
    }

    @Override
    public Mono<Void> shutdown() {
        // TODO: wait for the threads to finish.
        this.shutdownCts.cancel();
//        this.currentlyOwnedPartitions.clear();

        return Mono.empty();
    }

    private Mono<Void> loadLeases() {
        logger.debug("Starting renew leases assigned to this host on initialize.");

        return this.leaseContainer.getOwnedLeases()
            .flatMap( lease -> {
                logger.info("Lease with token {}: Acquired on startup.", lease.getLeaseToken());
                return this.addOrUpdateLease(lease);
            }).then();
    }

    private Mono<Void> removeLease(Lease lease) {
        return Mono.just(this)
            .flatMap(value -> {
                WorkerTask workerTask = this.currentlyOwnedPartitions.remove(lease.getLeaseToken());
                if (workerTask != null && workerTask.isRunning()) {
                    workerTask.cancelJob();
                }
                logger.info("Lease with token {}: released.", lease.getLeaseToken());

                return this.leaseManager.release(lease);
            })
            .onErrorResume(e -> {
                if (e instanceof LeaseLostException) {
                    logger.warn("Lease with token {}: lease already removed.", lease.getLeaseToken());
                } else {
                    logger.warn("Lease with token {}: failed to remove lease.", lease.getLeaseToken(), e);
                }

                return Mono.empty();
            })
            .doOnSuccess(aVoid -> {
                logger.info("Lease with token {}: successfully removed lease.", lease.getLeaseToken());
            });
    }

    private WorkerTask processPartition(PartitionSupervisor partitionSupervisor, Lease lease) {
        CancellationToken shutdownToken = this.shutdownCts.getToken();

        WorkerTask partitionSupervisorTask =
            new WorkerTask(
                lease,
                partitionSupervisor,
                getWorkerJob(partitionSupervisor, lease, shutdownToken));

        this.scheduler.schedule(partitionSupervisorTask);

        return partitionSupervisorTask;
    }

    private Mono<Void> getWorkerJob(
        PartitionSupervisor partitionSupervisor,
        Lease lease,
        CancellationToken shutdownToken) {
        return partitionSupervisor.run(shutdownToken)
            .onErrorResume(throwable -> {
                if (throwable instanceof FeedRangeGoneException) {
                    FeedRangeGoneException ex = (FeedRangeGoneException) throwable;
                    return this.handleFeedRangeGone(lease, ex.getLastContinuation());
                } else if (throwable instanceof TaskCancelledException) {
                    logger.debug("Lease with token {}: processing canceled.", lease.getLeaseToken());
                } else {
                    logger.warn("Lease with token {}: processing failed.", lease.getLeaseToken(), throwable);
                }

                return Mono.empty();
            })
            .then(this.removeLease(lease));
    }

    private Mono<Void> handleFeedRangeGone(Lease lease, String lastContinuationToken) {
        lease.setContinuationToken(lastContinuationToken);

        return this.synchronizer.getFeedRangeGoneHandler(lease)
                .flatMap(partitionGoneHandler -> {
                    return partitionGoneHandler.handlePartitionGone()
                            .flatMap(l -> {
                                l.setProperties(lease.getProperties());

                                // There is small caveat here:
                                // In the handling of merge for epkBased lease scenario, we are going to reuse the same lease.
                                // But if we have ever reached here, then usually it means the processor task and renewer task
                                // monitored by the partition supervisor have been cancelled.
                                // addOrUpdateLease(l) will not create new tasks in this case, which means there will be a short
                                // period there is no processing for the lease epk. But eventually the epk processing will be picked up
                                // again in the load balancing stage.
                                return this.addOrUpdateLease(l);
                            })
                            .then(this.tryDeleteGoneLease(lease, partitionGoneHandler.shouldDeleteCurrentLease()));
                })
                .onErrorResume(throwable -> {
                    logger.warn("Lease with token {}: failed to handle partition gone", lease.getLeaseToken(), throwable);
                    return  Mono.empty();
                });
    }

    private Mono<Void> tryDeleteGoneLease(Lease lease, boolean shouldRemoveLease) {
        if (shouldRemoveLease) {
            return this.leaseManager.delete(lease);
        }

        return Mono.empty();
    }
}
