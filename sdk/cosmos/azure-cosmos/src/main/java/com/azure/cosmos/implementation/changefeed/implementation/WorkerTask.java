// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.Lease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Worker task that executes in a separate thread.
 */
class WorkerTask extends Thread {
    private final Logger logger = LoggerFactory.getLogger(WorkerTask.class);
    private boolean done = false;
    private Mono<Void> job;
    private Lease lease;
    private CancellationToken taskCancellationToken;

    WorkerTask(Lease lease, CancellationToken taskCancellationToken, Mono<Void> job) {
        this.lease = lease;
        this.job = job;
        this.taskCancellationToken = taskCancellationToken;
    }

    @Override
    public void run() {
        job
            .doOnSuccess(avoid-> logger.info("Partition controller worker task {} has finished running.", lease.getLeaseToken()))
            .doOnTerminate(() -> {
                logger.info("Partition controller worker task {} has exited.", lease.getLeaseToken());
                job = null;
                this.done = true;
            })
            .subscribe();
    }

    @Override
    public void interrupt() {
        this.taskCancellationToken.cancel();
        super.interrupt();
    }

    public Lease lease() {
        return this.lease;
    }

    public boolean isRunning() {
        return !this.done;
    }
}
