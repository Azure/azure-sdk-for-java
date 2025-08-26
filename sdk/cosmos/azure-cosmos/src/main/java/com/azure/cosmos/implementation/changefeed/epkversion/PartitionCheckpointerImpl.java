// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Checkpoint the given partition up to the given continuation token.
 */
class PartitionCheckpointerImpl implements PartitionCheckpointer {
    private final Logger logger = LoggerFactory.getLogger(PartitionCheckpointerImpl.class);
    private final LeaseCheckpointer leaseCheckpointer;
    private Lease lease;
    private CancellationToken cancellationToken;

    public PartitionCheckpointerImpl(LeaseCheckpointer leaseCheckpointer, Lease lease) {
        this.leaseCheckpointer = leaseCheckpointer;
        this.lease = lease;
    }

    @Override
    public Mono<Lease> checkpointPartition(ChangeFeedState continuationState) {
        checkNotNull(continuationState, "Argument 'continuationState' must not be null.");
        checkArgument(
            continuationState.getContinuation().getContinuationTokenCount() == 1,
            "For ChangeFeedProcessor the continuation state should always have one range/continuation");

        if (cancellationToken.isCancellationRequested()) return Mono.error(new TaskCancelledException());

        return this.leaseCheckpointer.checkpoint(
            this.lease,
            //  Using the base64 encoded continuation token
            continuationState.toString(),
            cancellationToken)
            .map(lease1 -> {
                this.lease = lease1;
                logger.info("Checkpoint: partition {}, new continuation {}, owner {}", this.lease.getLeaseToken(),
                    this.lease.getReadableContinuationToken(), this.lease.getOwner());
                return lease1;
            });
    }

    @Override
    public PartitionCheckpointer setCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
