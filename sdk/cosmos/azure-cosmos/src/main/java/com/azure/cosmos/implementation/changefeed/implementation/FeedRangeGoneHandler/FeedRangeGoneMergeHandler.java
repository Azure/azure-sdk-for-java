// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.FeedRangeGoneHandler;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseCore;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseEpk;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FeedRangeGoneMergeHandler implements FeedRangeGoneHandler {
    private static final Logger logger = LoggerFactory.getLogger(FeedRangeGoneMergeHandler.class);
    private final Lease lease;
    private final PartitionKeyRange overlappingRange;
    private final LeaseManager leaseManager;
    private final AtomicBoolean removeCurrentLease;

    public FeedRangeGoneMergeHandler(Lease lease, PartitionKeyRange overlappingRange, LeaseManager leaseManager) {
        checkNotNull(lease, "Argument 'lease' can not be null");
        checkNotNull(overlappingRange, "Argument 'overlappingRange' can not be null");
        checkNotNull(leaseManager, "Argument 'leaseManager' can not be null");

        this.lease = lease;
        this.overlappingRange = overlappingRange;
        this.leaseManager = leaseManager;

        // A flag to indicate to upstream whether the current lease which we get FeedRangeGoneException should be removed.
        this.removeCurrentLease = new AtomicBoolean();
    }

    @Override
    public Flux<Lease> handlePartitionGone() {
        if (this.lease instanceof ServiceItemLeaseCore) {
            // Switch from partition based lease to epk based lease.
            // So need to remove the old partition based lease in the end.
            this.removeCurrentLease.set(true);
            return this.leaseManager.createLeaseIfNotExist((FeedRangeEpkImpl)this.lease.getFeedRange(), this.lease.getContinuationToken())
                    .doOnSuccess(
                            lease -> this.logger.info(
                                    "Lease with token {} redirected to partition {}",
                                    this.lease.getLeaseToken(),
                                    this.overlappingRange.getId()))
                    .flux();
        }

        if (lease instanceof ServiceItemLeaseEpk) {
            // The epk range just remapped to another partition
            // Will reuse the same lease, and keep draining by using the epk.
            this.removeCurrentLease.set(false);
            this.logger.info(
                    "Lease {} redirected to {}",
                    this.lease.getLeaseToken(),
                    this.overlappingRange.getId());
            return Flux.just(this.lease);
        }

        return Flux.error(new IllegalStateException("Lease type " + lease.getClass() + " is not supported"));
    }

    @Override
    public boolean shouldDeleteCurrentLease() {
        return this.removeCurrentLease.get();
    }
}
