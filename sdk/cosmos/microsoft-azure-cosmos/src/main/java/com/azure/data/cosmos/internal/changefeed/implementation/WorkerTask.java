// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.Lease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker task that executes in a separate thread.
 */
class WorkerTask extends Thread {
    private final Logger logger = LoggerFactory.getLogger(WorkerTask.class);
    private boolean done = false;
    private Runnable job;
    private Lease lease;

    WorkerTask(Lease lease, Runnable job) {
        this.lease = lease;
        this.job = job;
    }

    @Override
    public void run() {
        try {
            job.run();
            logger.info("Partition controller worker task {} has finished running.", lease.getLeaseToken());
        } finally {
            logger.info("Partition controller worker task {} has exited.", lease.getLeaseToken());
            job = null;
            this.done = true;
        }
    }

    public Lease lease() {
        return this.lease;
    }

    public boolean isRunning() {
        return !this.done;
    }
}
