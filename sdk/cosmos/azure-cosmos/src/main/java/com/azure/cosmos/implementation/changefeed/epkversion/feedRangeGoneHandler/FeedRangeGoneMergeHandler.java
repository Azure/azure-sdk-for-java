// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.Lease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FeedRangeGoneMergeHandler implements FeedRangeGoneHandler {
    private static final Logger logger = LoggerFactory.getLogger(FeedRangeGoneMergeHandler.class);
    private final Lease lease;
    private final PartitionKeyRange overlappingRange;
    private final AtomicBoolean removeCurrentLease;

    public FeedRangeGoneMergeHandler(Lease lease, PartitionKeyRange overlappingRange) {
        checkNotNull(lease, "Argument 'lease' can not be null");
        checkNotNull(overlappingRange, "Argument 'overlappingRange' can not be null");

        this.lease = lease;
        this.overlappingRange = overlappingRange;

        // A flag to indicate to upstream whether the current lease which we get FeedRangeGoneException should be removed.
        this.removeCurrentLease = new AtomicBoolean();
    }

    @Override
    public Flux<Lease> handlePartitionGone() {
        // The epk range just remapped to another partition
        // Will reuse the same lease, and keep draining by using the epk.
        this.removeCurrentLease.set(false);
        logger.info("Lease {} redirected to {}", this.lease.getLeaseToken(), this.overlappingRange.getId());
        return Flux.just(this.lease);
    }

    @Override
    public boolean shouldDeleteCurrentLease() {
        return this.removeCurrentLease.get();
    }
}
