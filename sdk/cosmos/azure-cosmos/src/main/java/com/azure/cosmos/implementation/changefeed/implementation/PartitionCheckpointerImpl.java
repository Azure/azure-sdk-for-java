// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Checkpoint the given partition up to the given continuation token.
 */
class PartitionCheckpointerImpl implements PartitionCheckpointer {
    private final Logger logger = LoggerFactory.getLogger(PartitionCheckpointerImpl.class);
    private final LeaseCheckpointer leaseCheckpointer;
    private Lease lease;

    public PartitionCheckpointerImpl(LeaseCheckpointer leaseCheckpointer, Lease lease) {
        this.leaseCheckpointer = leaseCheckpointer;
        this.lease = lease;
    }

    @Override
    public Mono<Lease> checkpointPartition(String continuationToken) {
        return this.leaseCheckpointer.checkpoint(this.lease, continuationToken)
            .map(lease1 -> {
                this.lease = lease1;
                logger.info("Checkpoint: partition {}, new continuation {}", this.lease.getLeaseToken(), this.lease.getContinuationToken());
                return lease1;
            });
    }
}
