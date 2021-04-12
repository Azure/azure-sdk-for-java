// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Worker task that executes in a separate thread.
 */
class WorkerTask extends Thread {
    private final Logger logger = LoggerFactory.getLogger(WorkerTask.class);
    private AtomicBoolean done;
    private Mono<Void> job;
    private Lease lease;
    private PartitionSupervisor partitionSupervisor;

    WorkerTask(Lease lease, PartitionSupervisor partitionSupervisor, Mono<Void> job) {
        this.lease = lease;
        this.job = job;
        this.partitionSupervisor = partitionSupervisor;
        done = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        job
            .doOnSuccess(avoid -> logger.info("Partition controller worker task {} has finished running.", lease.getLeaseToken()))
            .doOnTerminate(() -> {
                logger.info("Partition controller worker task {} has exited.", lease.getLeaseToken());
                job = null;
                this.done.set(true);
            })
            .subscribe();
    }

    public void cancelJob() {
        this.partitionSupervisor.shutdown();
        this.interrupt();
    }

    public Lease lease() {
        return this.lease;
    }

    public boolean isRunning() {
        return !this.done.get();
    }
}
