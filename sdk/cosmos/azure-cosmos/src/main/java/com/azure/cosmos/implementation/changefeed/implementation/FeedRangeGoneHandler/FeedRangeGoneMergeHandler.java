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

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FeedRangeGoneMergeHandler implements FeedRangeGoneHandler {
    private static final Logger logger = LoggerFactory.getLogger(FeedRangeGoneMergeHandler.class);
    private final Lease lease;
    private final PartitionKeyRange overlappingRange;
    private final LeaseManager leaseManager;
    private final boolean removeCurrentLease;

    public FeedRangeGoneMergeHandler(Lease lease, PartitionKeyRange overlappingRange, LeaseManager leaseManager) {
        checkNotNull(lease, "Argument 'lease' can not be null");
        checkNotNull(overlappingRange, "Argument 'overlappingRange' can not be null");
        checkNotNull(leaseManager, "Argument 'leaseManager' can not be null");

        this.lease = lease;
        this.overlappingRange = overlappingRange;
        this.leaseManager = leaseManager;

        if (lease instanceof ServiceItemLeaseCore) {
            this.removeCurrentLease = true;
        } else if (lease instanceof ServiceItemLeaseEpk) {
            this.removeCurrentLease = false;
        } else {
            throw new IllegalArgumentException("Lease type " + lease.getClass() + " is not supported");
        }
    }

    @Override
    public Flux<Lease> handlePartitionGone() {
        if (this.lease instanceof ServiceItemLeaseCore) {
            // Switch from partition based lease to epk based lease.
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
            this.logger.info(
                    "Lease {} redirected to {}",
                    this.lease.getLeaseToken(),
                    this.overlappingRange.getId());
            return Flux.just(this.lease);
        }

        return Flux.error(new IllegalStateException("Lease type " + lease.getClass() + " is not supported"));
    }

    @Override
    public boolean shouldRemoveGoneLease() {
        return this.removeCurrentLease;
    }
}
