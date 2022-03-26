// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.FeedRangeGoneHandler;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseCore;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseEpk;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FeedRangeGoneSplitHandler implements FeedRangeGoneHandler {
    private static final Logger logger = LoggerFactory.getLogger(FeedRangeGoneSplitHandler.class);
    private final Lease lease;
    private final List<PartitionKeyRange> overlappingRanges;
    private final LeaseManager leaseManager;
    private final boolean removeCurrentLease;

    public FeedRangeGoneSplitHandler(Lease lease, List<PartitionKeyRange> overlappingRanges, LeaseManager leaseManager) {
        checkNotNull(lease, "Argument 'lease' can not be null");
        checkNotNull(overlappingRanges, "Argument 'overlappingRanges' can not be null");
        checkNotNull(leaseManager, "Argument 'leaseManager' can not be null");

        this.lease = lease;
        this.overlappingRanges = overlappingRanges;
        this.leaseManager = leaseManager;
        this.removeCurrentLease = true;
    }

    @Override
    public Flux<Lease> handlePartitionGone() {
        if (this.lease instanceof ServiceItemLeaseCore) {
            return this.handlePartitionGone((ServiceItemLeaseCore) lease);
        }
        if (this.lease instanceof ServiceItemLeaseEpk) {
            return this.handlePartitionGone((ServiceItemLeaseEpk) lease);
        }

        return Flux.error(new IllegalStateException("Lease type " + this.lease.getClass() + " is not supported"));
    }

    private Flux<Lease> handlePartitionGone(ServiceItemLeaseCore partitionBasedLease) {
        checkNotNull(partitionBasedLease, "Argument 'partitionBasedLease' can not be null");

        List<String> leaseTokens = new ArrayList<>();
        return Flux.fromIterable(overlappingRanges)
                .flatMap(pkRange -> {
                    return this.leaseManager.createLeaseIfNotExist(pkRange, this.lease.getContinuationToken())
                            .doOnSuccess(lease -> leaseTokens.add(lease.getLeaseToken()));
                })
                .doOnComplete(() -> {
                    this.logger.info("Lease with token {} split into {}", this.lease.getLeaseToken(), StringUtils.join(leaseTokens, ","));
                });
    }

    private Flux<Lease> handlePartitionGone(ServiceItemLeaseEpk epkBasedLease) {
        checkNotNull(epkBasedLease, "Argument 'epkBasedLease' can not be null");

        FeedRangeEpkImpl splitRange = (FeedRangeEpkImpl) epkBasedLease.getFeedRange();
        AtomicReference<String> min = new AtomicReference<>(splitRange.getRange().getMin());
        AtomicReference<String> max = new AtomicReference<>(splitRange.getRange().getMax());

        List<String> leaseTokens = new ArrayList<>();

        // Create new leases starting from the current min and ending in the current max and across the ordered list of partitions
        // Example:
        // Current lease epk range: AA-DD
        // It split into 3 ranges: []-BB, BB-CC, CC-[]
        // So we will create EPKRange lease for AA-BB, BB-CC, CC-DD
        return Flux.fromIterable(overlappingRanges)
                .map(pkRange -> {
                    Range<String> range = pkRange.toRange();
                    // TODO: Annie: what happens if only one of the lease is created successfully
                    Range<String> mergedRange = new Range<>(min.get(), range.getMax(), true, false);
                    FeedRangeEpkImpl newFeedRangeEpkImpl = new FeedRangeEpkImpl(mergedRange);
                    min.set(pkRange.getMaxExclusive());

                    return newFeedRangeEpkImpl;
                })
                // add the last range with the original max and the last min
                .concatWithValues(new FeedRangeEpkImpl(new Range<>(min.get(), max.get(), true, false)))
                .flatMap(feedRangeEpkImpl -> {
                    return this.leaseManager.createLeaseIfNotExist(feedRangeEpkImpl, this.lease.getContinuationToken())
                            .doOnSuccess(newLease -> leaseTokens.add(newLease.getLeaseToken()));
                })
                .doOnComplete(() -> {
                    this.logger.info("Lease with token {} split into {}", this.lease.getLeaseToken(), StringUtils.join(leaseTokens, ","));
                });

    }

    @Override
    public boolean isRemoveCurrentLease() {
        return this.removeCurrentLease;
    }
}
